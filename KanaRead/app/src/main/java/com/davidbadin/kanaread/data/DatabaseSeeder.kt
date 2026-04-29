package com.davidbadin.kanaread.data

/**
 * One-shot seeder that populates the Room database from the
 * in-memory word lists in [HiraganaWords] and [KatakanaWords].
 *
 * Seeding only runs if the words table is empty so we don't insert
 * duplicates on subsequent launches.
 */
object DatabaseSeeder {

    suspend fun seedIfNeeded(database: KanaDatabase) {
        val dao = database.wordDao()
        if (dao.count() > 0) return

        val all = mutableListOf<WordEntity>()

        HiraganaWords.LIST.forEach { (kana, romaji, english) ->
            all += WordEntity(
                kana = kana,
                romaji = romaji,
                english = english,
                type = "hiragana"
            )
        }

        KatakanaWords.LIST.forEach { (kana, romaji, english) ->
            all += WordEntity(
                kana = kana,
                romaji = romaji,
                english = english,
                type = "katakana"
            )
        }

        // Insert in batches to keep memory pressure low.
        all.chunked(500).forEach { batch ->
            dao.insertAll(batch)
        }
    }
}
