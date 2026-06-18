package sk.punkacidetom.pd2026.feature.spotify

import com.spotify.protocol.types.PlayerState

sealed interface SpotifyUiState {
    /** Checking Spotify installation / waiting for App Remote to connect */
    data object Connecting : SpotifyUiState

    /** App Remote connected and delivering player state */
    data class SdkConnected(val playerState: PlayerState) : SpotifyUiState

    /** Spotify not installed or SDK connection failed — show WebView iframe */
    data object FallbackWebView : SpotifyUiState
}
