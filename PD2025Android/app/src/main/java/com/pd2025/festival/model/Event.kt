package com.pd2025.festival.model

import java.util.Date

data class Event(
    val id: String,
    val band: String,
    val start: Date,
    val end: Date,
    val stage: String,           // "A" or "B"
    val shortDescription: String,
    val description: String,
    val spotUrl: String?,
    val genre: String?,
    var favorite: Boolean = false
)

data class Stage(
    val name: String,
    val id: String
)

data class FestivalDay(
    val name: String,
    val number: Int
)
