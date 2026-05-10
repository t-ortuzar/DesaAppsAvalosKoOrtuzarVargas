package com.example.desaappsavaloskoortuzarvargas.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class News(
    val id: Int,
    val title: String,
    val content: String,
    val imageUrl: String,
    val date: String,
    val gameId: Int?,
    val platform: String,
    val category: String // "discount", "release", "update", "event"
)

