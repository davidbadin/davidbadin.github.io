package sk.punkacidetom.pd2026.core.i18n.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object I18nModule
// LocaleHelper is @Singleton + @Inject constructor — Hilt picks it up automatically.
