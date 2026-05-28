package sk.punkacidetom.pd2026.core.data.cache

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sk.punkacidetom.pd2026.core.model.Band
import java.io.File
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BandCache @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val cacheFile = File(context.filesDir, "band_cache.json")
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter)
        .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter)
        .create()

    suspend fun loadBands(): List<Band> = withContext(Dispatchers.IO) {
        if (!cacheFile.exists()) return@withContext emptyList()
        try {
            val json = cacheFile.readText()
            val type = object : TypeToken<List<BandJson>>() {}.type
            val list: List<BandJson> = gson.fromJson(json, type) ?: emptyList()
            list.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveBands(bands: List<Band>): Unit = withContext(Dispatchers.IO) {
        val list = bands.map { BandJson.fromDomain(it) }
        cacheFile.writeText(gson.toJson(list))
    }

    private object LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        override fun serialize(src: LocalDate, typeOfSrc: Type, ctx: JsonSerializationContext) =
            JsonPrimitive(src.toString())

        override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): LocalDate =
            LocalDate.parse(json.asString)
    }

    private object LocalTimeAdapter : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
        override fun serialize(src: LocalTime, typeOfSrc: Type, ctx: JsonSerializationContext) =
            JsonPrimitive(src.toString())

        override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): LocalTime =
            LocalTime.parse(json.asString)
    }
}

// Flat JSON-serializable mirror of Band to avoid Gson issues with java.time types
private data class BandJson(
    val id: Int,
    val name: String,
    val description: String,
    val descriptionEn: String,
    val stageCode: String,
    val spotifyArtistId: String,
    val genre: String,
    val sortingPriority: Int?,
    val imageName: String,
    val startDate: String,
    val startTime: String,
    val endDate: String,
    val endTime: String,
) {
    fun toDomain() = Band(
        id = id, name = name, description = description, descriptionEn = descriptionEn,
        stageCode = stageCode, spotifyArtistId = spotifyArtistId, genre = genre,
        sortingPriority = sortingPriority, imageName = imageName,
        startDate = java.time.LocalDate.parse(startDate),
        startTime = java.time.LocalTime.parse(startTime),
        endDate = java.time.LocalDate.parse(endDate),
        endTime = java.time.LocalTime.parse(endTime),
    )

    companion object {
        fun fromDomain(b: Band) = BandJson(
            id = b.id, name = b.name, description = b.description, descriptionEn = b.descriptionEn,
            stageCode = b.stageCode, spotifyArtistId = b.spotifyArtistId, genre = b.genre,
            sortingPriority = b.sortingPriority, imageName = b.imageName,
            startDate = b.startDate.toString(), startTime = b.startTime.toString(),
            endDate = b.endDate.toString(), endTime = b.endTime.toString(),
        )
    }
}
