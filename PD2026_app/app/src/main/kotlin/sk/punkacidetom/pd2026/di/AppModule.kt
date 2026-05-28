package sk.punkacidetom.pd2026.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sk.punkacidetom.pd2026.BuildConfig
import sk.punkacidetom.pd2026.core.data.di.SheetGid
import sk.punkacidetom.pd2026.core.data.di.SheetId

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @SheetId
    fun provideSheetId(): String = BuildConfig.SHEET_ID

    @Provides
    @SheetGid
    fun provideSheetGid(): String = BuildConfig.SHEET_GID
}
