package com.example.desaappsavaloskoortuzarvargas.data.remote

import com.example.desaappsavaloskoortuzarvargas.domain.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Auth service backed by Firebase Authentication + Firestore.
 * Usernames are stored as synthetic emails: {username}@argengamer.app
 * User profiles and ALL preferences are stored in Firestore: users/{uid}
 */
class FirebaseAuthService {

    companion object {
        private const val EMAIL_SUFFIX = "@argengamer.app"
        private const val USERS_COLLECTION = "users"

        fun friendlyError(e: Throwable): String {
            val msg = e.message?.lowercase() ?: ""
            return when {
                msg.contains("email address is already in use") || msg.contains("already in use") ->
                    "Ese nombre de usuario ya está en uso."
                msg.contains("there is no user record") || msg.contains("no user record") ||
                msg.contains("wrong password") || msg.contains("invalid credential") ->
                    "Usuario o contraseña incorrectos."
                msg.contains("password should be at least") || msg.contains("weak-password") ->
                    "La contraseña debe tener al menos 6 caracteres."
                msg.contains("network") || msg.contains("timeout") ->
                    "Error de red. Verificá tu conexión e intentá de nuevo."
                msg.contains("username and password required") ->
                    "Ingresá un nombre de usuario y contraseña."
                else -> e.message ?: "Error desconocido. Intentá de nuevo."
            }
        }
    }

    private val auth: FirebaseAuth get() = Firebase.auth
    private val db: FirebaseFirestore get() = Firebase.firestore("argengamer")

    private fun usernameToEmail(username: String) = "$username$EMAIL_SUFFIX"

    suspend fun register(username: String, password: String): Result<AppUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val lower = username.lowercase().trim()
                require(lower.isNotEmpty() && password.isNotEmpty()) { "Username and password required" }
                require(password.length >= 6) { "Password should be at least 6 characters" }

                val result = auth.createUserWithEmailAndPassword(usernameToEmail(lower), password).await()
                val uid    = result.user?.uid ?: error("Firebase returned null UID")
                val now    = System.currentTimeMillis()

                val defaultDoc = mapOf(
                    "username" to lower,
                    "email" to "",
                    "displayName" to "",
                    "favorites" to emptyList<Int>(),
                    "darkMode" to true,
                    "languageCode" to "en",
                    "country" to "Argentina",
                    "countryCode" to "AR",
                    "globalNotifications" to true,
                    "createdAt" to now
                )
                try {
                    db.collection(USERS_COLLECTION).document(uid).set(defaultDoc).await()
                } catch (_: Exception) { }

                AppUser(id = uid, username = lower, createdAt = now)
            }
        }

    suspend fun login(username: String, password: String): Result<AppUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val lower = username.lowercase().trim()
                require(lower.isNotEmpty() && password.isNotEmpty()) { "Username and password required" }

                val result = auth.signInWithEmailAndPassword(usernameToEmail(lower), password).await()
                val uid    = result.user?.uid ?: error("Firebase returned null UID")

                val doc  = db.collection(USERS_COLLECTION).document(uid).get().await()
                docToAppUser(uid, lower, doc)
            }
        }

    suspend fun getUserById(userId: String): Result<AppUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val doc = db.collection(USERS_COLLECTION).document(userId).get().await()
                if (!doc.exists()) error("User not found")
                docToAppUser(userId, doc.getString("username") ?: "", doc)
            }
        }

    /**
     * Sync ALL user data (preferences + favorites) to Firestore in a single write.
     * Uses set+merge so existing fields not included here are preserved.
     */
    suspend fun syncAllUserData(
        userId: String,
        displayName: String,
        email: String,
        favoriteGameIds: List<Int>,
        darkMode: Boolean,
        languageCode: String,
        country: String,
        countryCode: String,
        globalNotifications: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val data = mapOf(
                "displayName" to displayName,
                "email" to email,
                "favorites" to favoriteGameIds,
                "darkMode" to darkMode,
                "languageCode" to languageCode,
                "country" to country,
                "countryCode" to countryCode,
                "globalNotifications" to globalNotifications
            )
            db.collection(USERS_COLLECTION).document(userId)
                .set(data, SetOptions.merge()).await()
            Unit
        }
    }

    private fun docToAppUser(
        userId: String,
        fallbackUsername: String,
        doc: com.google.firebase.firestore.DocumentSnapshot
    ): AppUser = AppUser(
        id                  = userId,
        username            = doc.getString("username") ?: fallbackUsername,
        email               = doc.getString("email") ?: "",
        favoriteGameIds     = parseFavs(doc.get("favorites")),
        displayName         = doc.getString("displayName") ?: "",
        darkMode            = doc.getBoolean("darkMode") ?: true,
        languageCode        = doc.getString("languageCode") ?: "en",
        country             = doc.getString("country") ?: "Argentina",
        countryCode         = doc.getString("countryCode") ?: "AR",
        globalNotifications = doc.getBoolean("globalNotifications") ?: true,
        createdAt           = doc.getLong("createdAt") ?: 0L
    )

    private fun parseFavs(raw: Any?): List<Int> =
        (raw as? List<*>)?.mapNotNull { (it as? Long)?.toInt() ?: it as? Int } ?: emptyList()

    fun close() { }
}