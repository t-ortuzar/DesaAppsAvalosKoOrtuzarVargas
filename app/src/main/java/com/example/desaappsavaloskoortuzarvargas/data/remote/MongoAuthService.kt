package com.example.desaappsavaloskoortuzarvargas.data.remote

import com.example.desaappsavaloskoortuzarvargas.domain.model.MongoUser
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.bson.types.ObjectId
import java.security.MessageDigest

/**
 * Service for user authentication and favorites synchronisation via MongoDB Atlas.
 *
 * Database : gametracker
 * Collection: users
 *
 * User document structure:
 * {
 *   _id      : ObjectId,
 *   username : String  (unique, lowercase),
 *   pwdHash  : String  (SHA-256 hex),
 *   email    : String,
 *   favorites: [ Int, … ],
 *   createdAt: Long
 * }
 */
class MongoAuthService {

    companion object {
        // Standard mongodb:// URI with resolved SRV hosts — avoids JNDI (javax.naming)
        // which is not available on Android.
        // Hosts resolved from: _mongodb._tcp.cluster0.cwjfiaw.mongodb.net (SRV)
        // Options resolved from: cluster0.cwjfiaw.mongodb.net (TXT)
        private const val CONNECTION_STRING =
            "mongodb://tomasortuzar98_db_user:077bGV18snBCQcnF@" +
            "ac-7ua8lo7-shard-00-00.cwjfiaw.mongodb.net:27017," +
            "ac-7ua8lo7-shard-00-01.cwjfiaw.mongodb.net:27017," +
            "ac-7ua8lo7-shard-00-02.cwjfiaw.mongodb.net:27017" +
            "/gametracker?authSource=admin&replicaSet=atlas-3x2dvl-shard-0" +
            "&tls=true&retryWrites=true&w=majority" +
            "&connectTimeoutMS=15000&socketTimeoutMS=15000&serverSelectionTimeoutMS=15000"
        private const val DB_NAME   = "gametracker"
        private const val COLL_NAME = "users"

        /**
         * Translate low-level MongoDB/network exceptions into user-friendly messages.
         * Port 27017 is often blocked by mobile carriers — detect this and guide the user.
         */
        fun friendlyError(e: Throwable): String {
            val msg = e.message?.lowercase() ?: ""
            val cause = e.cause?.message?.lowercase() ?: ""
            return when {
                msg.contains("timeout") || msg.contains("timed out") ||
                cause.contains("timeout") || cause.contains("timed out") ->
                    "No se pudo conectar al servidor (tiempo de espera agotado). " +
                    "El puerto 27017 puede estar bloqueado en tu red móvil. " +
                    "Intentá con WiFi."
                msg.contains("connection refused") || cause.contains("connection refused") ||
                msg.contains("connect") && (msg.contains("refuse") || cause.contains("refuse")) ->
                    "Conexión rechazada. Verificá tu conexión a Internet o intentá con WiFi."
                msg.contains("ssl") || msg.contains("tls") ||
                cause.contains("ssl") || cause.contains("tls") ->
                    "Error de seguridad (TLS). Verificá la fecha/hora del dispositivo."
                msg.contains("unauthorized") || msg.contains("authentication") ->
                    "Error de autenticación con la base de datos."
                msg.contains("username already taken") ->
                    "Ese nombre de usuario ya está en uso."
                msg.contains("username and password required") ->
                    "Ingresá un nombre de usuario y contraseña."
                msg.contains("invalid credentials") ->
                    "Usuario o contraseña incorrectos."
                else -> e.message ?: "Error desconocido. Intentá de nuevo."
            }
        }
    }

    // Lazy-init MongoClient — created once per process on first use (IO thread)
    private val client by lazy { MongoClients.create(CONNECTION_STRING) }
    private val collection: MongoCollection<Document>
        get() = client.getDatabase(DB_NAME).getCollection(COLL_NAME)

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.fold("") { acc, b -> acc + "%02x".format(b) }
    }

    private fun Document.toMongoUser(): MongoUser {
        @Suppress("UNCHECKED_CAST")
        val favList = (get("favorites") as? List<*>)?.mapNotNull { (it as? Int) } ?: emptyList()
        return MongoUser(
            id               = getObjectId("_id")?.toHexString() ?: getString("_id") ?: "",
            username         = getString("username") ?: "",
            email            = getString("email") ?: "",
            favoriteGameIds  = favList,
            createdAt        = getLong("createdAt") ?: 0L
        )
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Register a new user. Returns the created [MongoUser] on success.
     * Throws if the username already exists.
     */
    suspend fun register(username: String, password: String): Result<MongoUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val lower = username.lowercase().trim()
                require(lower.isNotEmpty() && password.isNotEmpty()) { "Username and password required" }

                // Check for existing username
                val existing = collection.find(Filters.eq("username", lower)).first()
                require(existing == null) { "Username already taken" }

                val doc = Document()
                    .append("username", lower)
                    .append("pwdHash",  hash(password))
                    .append("email",    "")
                    .append("favorites", emptyList<Int>())
                    .append("createdAt", System.currentTimeMillis())

                collection.insertOne(doc)
                doc.toMongoUser()
            }
        }

    /**
     * Log in an existing user. Returns the [MongoUser] on success.
     */
    suspend fun login(username: String, password: String): Result<MongoUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val lower = username.lowercase().trim()
                require(lower.isNotEmpty() && password.isNotEmpty()) { "Username and password required" }

                val doc = collection.find(
                    Filters.and(
                        Filters.eq("username", lower),
                        Filters.eq("pwdHash",  hash(password))
                    )
                ).first() ?: error("Invalid credentials")

                doc.toMongoUser()
            }
        }

    /**
     * Fetch a user document by their stored ID (hex string from ObjectId).
     */
    suspend fun getUserById(userId: String): Result<MongoUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val oid = ObjectId(userId)
                collection.find(Filters.eq("_id", oid)).first()
                    ?.toMongoUser()
                    ?: error("User not found")
            }
        }

    /**
     * Push the current local favorites list to MongoDB for the given user.
     */
    suspend fun syncFavorites(userId: String, favoriteGameIds: List<Int>): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val oid = ObjectId(userId)
                collection.updateOne(
                    Filters.eq("_id", oid),
                    Updates.set("favorites", favoriteGameIds)
                )
                Unit
            }
        }

    /**
     * Pull the favorites list stored in MongoDB for the given user.
     */
    suspend fun fetchFavorites(userId: String): Result<List<Int>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val oid = ObjectId(userId)
                val doc = collection.find(Filters.eq("_id", oid)).first()
                    ?: return@runCatching emptyList()
                @Suppress("UNCHECKED_CAST")
                (doc.get("favorites") as? List<*>)?.mapNotNull { it as? Int } ?: emptyList()
            }
        }

    fun close() {
        runCatching { client.close() }
    }
}


