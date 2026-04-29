package com.davidbadin.kanaread.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object for words.
 */
@Dao
interface WordDao {

    @Query("SELECT * FROM words WHERE type = :type")
    suspend fun getWordsByType(type: String): List<WordEntity>

    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)
}
