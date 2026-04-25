package com.pd2025.festival.network

import android.content.Context
import com.pd2025.festival.model.Constants
import com.pd2025.festival.model.Event
import com.pd2025.festival.utils.DateUtils
import com.pd2025.festival.utils.PrefsHelper

class EventRepository(private val context: Context) {

    suspend fun fetchEvents(): Result<List<Event>> {
        return try {
            val range = "'PD2025'!A2:H100"
            val response = ApiClient.sheetsService.getSheetData(
                sheetId = Constants.SHEET_ID,
                range = range,
                apiKey = Constants.API_KEY
            )

            if (response.isSuccessful) {
                val body = response.body()
                val valuesArray = body?.getAsJsonArray("values")
                    ?: return Result.failure(Exception("No data"))

                val favorites = PrefsHelper.loadFavorites(context)
                val events = mutableListOf<Event>()

                for (i in 0 until valuesArray.size()) {
                    try {
                        val row = valuesArray[i].asJsonArray
                        if (row.size() < 7) continue

                        val startStr = row[0].asString
                        val endStr = row[1].asString
                        val band = row[2].asString
                        val description = if (row.size() > 3) row[3].asString else ""
                        val stageId = row[4].asString
                        val spotUrl = if (row.size() > 5) row[5].asString.takeIf { it.isNotBlank() } else null
                        val id = row[6].asString
                        val genre = if (row.size() > 7) row[7].asString.takeIf { it.isNotBlank() } else null

                        val startDate = DateUtils.parseDate(startStr) ?: continue
                        val endDate = DateUtils.parseDate(endStr) ?: continue

                        val stage = Constants.STAGES.find { it.id == stageId } ?: continue
                        val shortDescr = DateUtils.formatShortDescr(stage.name, startDate, endDate)

                        events.add(
                            Event(
                                id = id,
                                band = band,
                                start = startDate,
                                end = endDate,
                                stage = stageId,
                                shortDescription = shortDescr,
                                description = description,
                                spotUrl = spotUrl,
                                genre = genre,
                                favorite = favorites.contains(id)
                            )
                        )
                    } catch (e: Exception) {
                        // skip malformed rows
                        continue
                    }
                }

                events.sortBy { it.start }
                PrefsHelper.saveEvents(context, events)
                Result.success(events)

            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loadCachedEvents(): List<Event>? {
        val cached = PrefsHelper.loadEvents(context) ?: return null
        val favorites = PrefsHelper.loadFavorites(context)
        return cached.map { it.copy(favorite = favorites.contains(it.id)) }
    }
}
