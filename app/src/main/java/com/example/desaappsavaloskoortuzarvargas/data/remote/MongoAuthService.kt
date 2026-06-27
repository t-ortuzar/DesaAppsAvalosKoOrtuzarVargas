package com.example.desaappsavaloskoortuzarvargas.data.remote

import com.example.desaappsavaloskoortuzarvargas.domain.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Auth service backed by Firebase Authentication + Firestore.
 * Usernames are stored as synthetic emails: {username}@argengamer.app
 * User profiles and favourites are stored in Firestore: users/{uid}
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
    // Named Firestore database — created with ID "argengamer" in Firebase Console
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

                // Write profile to Firestore — non-fatal if it fails (user is already authenticated)
                try {
                    db.collection(USERS_COLLECTION).document(uid).set(
                        mapOf("username" to lower, "email" to "", "favorites" to emptyList<Int>(), "createdAt" to now)
                    ).await()
                } catch (_: Exception) { /* profile write failed — sync will retry on next login */ }

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
                val favs = parseFavs(doc.get("favorites"))

                AppUser(
                    id              = uid,
                    username        = doc.getString("username") ?: lower,
                    email           = doc.getString("email") ?: "",
                    favoriteGameIds = favs,
                    createdAt       = doc.getLong("createdAt") ?: 0L
                )
            }
        }

    suspend fun getUserById(userId: String): Result<AppUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val doc = db.collection(USERS_COLLECTION).document(userId).get().await()
                if (!doc.exists()) error("User not found")
                AppUser(
                    id              = userId,
                    username        = doc.getString("username") ?: "",
                    email           = doc.getString("email") ?: "",
                    favoriteGameIds = parseFavs(doc.get("favorites")),
                    createdAt       = doc.getLong("createdAt") ?: 0L
                )
            }
        }

    suspend fun syncFavorites(userId: String, favoriteGameIds: List<Int>): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                db.collection(USERS_COLLECTION).document(userId)
                    .update("favorites", favoriteGameIds).await()
                Unit
            }
        }

    suspend fun fetchFavorites(userId: String): Result<List<Int>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val doc = db.collection(USERS_COLLECTION).document(userId).get().await()
                parseFavs(doc.get("favorites"))
            }
        }

    private fun parseFavs(raw: Any?): List<Int> =
        (raw as? List<*>)?.mapNotNull { (it as? Long)?.toInt() ?: it as? Int } ?: emptyList()

    fun close() { }
}