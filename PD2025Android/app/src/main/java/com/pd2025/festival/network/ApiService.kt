package com.pd2025.festival.network

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SheetsApiService {
    @GET("{sheetId}/values/{range}")
    suspend fun getSheetData(
        @Path("sheetId") sheetId: String,
        @Path("range") range: String,
        @Query("key") apiKey: String
    ): Response<JsonObject>
}

object ApiClient {
    private const val BASE_URL = "https://sheets.googleapis.com/v4/spreadsheets/"

    val sheetsService: SheetsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SheetsApiService::class.java)
    }
}
