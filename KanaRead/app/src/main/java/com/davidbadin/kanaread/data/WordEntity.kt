package com.davidbadin.kanaread.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single word entry stored in the Room database.
 *
 * type is either "hiragana" or "katakana".
 */
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val kana: String,
    val romaji: String,
    val english: String,
    val type: String
)
