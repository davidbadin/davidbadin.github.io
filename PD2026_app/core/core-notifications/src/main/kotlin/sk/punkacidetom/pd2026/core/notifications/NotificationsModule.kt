package sk.punkacidetom.pd2026.core.notifications

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sk.punkacidetom.pd2026.core.model.BandNotificationScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    @Singleton
    abstract fun bindBandNotificationScheduler(
        impl: BandNotificationSchedulerImpl,
    ): BandNotificationScheduler
}
