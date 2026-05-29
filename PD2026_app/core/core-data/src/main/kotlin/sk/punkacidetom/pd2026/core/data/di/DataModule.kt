package sk.punkacidetom.pd2026.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import sk.punkacidetom.pd2026.core.data.repository.BandRepository
import sk.punkacidetom.pd2026.core.data.repository.BandRepositoryImpl
import sk.punkacidetom.pd2026.core.data.remote.CsvSheetFetcher
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pd2026_prefs")

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindBandRepository(impl: BandRepositoryImpl): BandRepository

    companion object {

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> =
            ctx.dataStore

        @Provides
        @Singleton
        fun provideCsvSheetFetcher(
            client: OkHttpClient,
            @SheetId sheetId: String,
            @SheetGid sheetGid: String,
        ): CsvSheetFetcher = CsvSheetFetcher(client, sheetId, sheetGid)
    }
}
