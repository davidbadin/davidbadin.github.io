package com.davidbadin.kanaread.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database holding all kana words.
 *
 * The database file is created on first access and seeded on first launch
 * by [DatabaseSeeder].
 */
@Database(entities = [WordEntity::class], version = 1, exportSchema = false)
abstract class KanaDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    companion object {
        private const val DB_NAME = "kana_read.db"

        @Volatile
        private var INSTANCE: KanaDatabase? = null

        fun getInstance(context: Context): KanaDatabase {
            return INSTANCE ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    KanaDatabase::class.java,
                    DB_NAME
                ).build()
                INSTANCE = newInstance
                newInstance
            }
        }
    }
}
