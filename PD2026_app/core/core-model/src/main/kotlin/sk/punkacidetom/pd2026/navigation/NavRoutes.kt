package sk.punkacidetom.pd2026.navigation

import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object TimetableRoute
@Serializable object BandsRoute
@Serializable data class BandDetailRoute(val bandId: Int)
@Serializable object NewsRoute
@Serializable object InfoRoute
@Serializable object TicketsRoute
@Serializable object SettingsRoute
@Serializable object SpotifyRoute
