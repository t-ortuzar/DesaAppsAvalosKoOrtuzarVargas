package com.example.desaappsavaloskoortuzarvargas.domain.model

data class MongoUser(
    val id: String,
    val username: String,
    val email: String = "",
    val favoriteGameIds: List<Int> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

