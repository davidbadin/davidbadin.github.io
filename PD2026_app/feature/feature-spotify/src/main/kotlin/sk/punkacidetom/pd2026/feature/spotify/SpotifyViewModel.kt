package sk.punkacidetom.pd2026.feature.spotify

import android.content.Context
import androidx.lifecycle.ViewModel
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sk.punkacidetom.pd2026.feature.spotify.util.SpotifyLauncher
import javax.inject.Inject

@HiltViewModel
class SpotifyViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SpotifyUiState>(SpotifyUiState.Connecting)
    val uiState: StateFlow<SpotifyUiState> = _uiState.asStateFlow()

    private var appRemote: SpotifyAppRemote? = null

    /**
     * Attempts to connect to the Spotify App Remote and immediately start playing [spotifyUri]
     * (e.g. `"spotify:playlist:5QL8HJ0..."` or `"spotify:artist:3tvOPc..."`).
     * Falls back to [SpotifyUiState.FallbackWebView] if Spotify is not installed or the SDK
     * connection fails (e.g. user is not logged in, SHA-1 mismatch, non-Premium account quirks).
     */
    fun connect(spotifyUri: String) {
        // Always clean up any existing connection first
        appRemote?.let { SpotifyAppRemote.disconnect(it) }
        appRemote = null

        if (!SpotifyLauncher.isSpotifyInstalled(context)) {
            _uiState.value = SpotifyUiState.FallbackWebView
            return
        }
        _uiState.value = SpotifyUiState.Connecting

        // Timeout — fall back to WebView if SDK doesn't respond within 10 s
        viewModelScope.launch {
            delay(10_000)
            if (_uiState.value is SpotifyUiState.Connecting) {
                _uiState.value = SpotifyUiState.FallbackWebView
            }
        }

        val params = ConnectionParams.Builder(BuildConfig.SPOTIFY_CLIENT_ID)
            .setRedirectUri(BuildConfig.SPOTIFY_REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, params, object : Connector.ConnectionListener {
            override fun onConnected(remote: SpotifyAppRemote) {
                appRemote = remote
                remote.playerApi.play(spotifyUri)
                remote.playerApi.subscribeToPlayerState().setEventCallback { state ->
                    _uiState.value = SpotifyUiState.SdkConnected(state)
                }
            }

            override fun onFailure(error: Throwable) {
                _uiState.value = SpotifyUiState.FallbackWebView
            }
        })
    }

    fun togglePlayPause() {
        val remote = appRemote ?: return
        val state = (_uiState.value as? SpotifyUiState.SdkConnected)?.playerState ?: return
        if (state.isPaused) remote.playerApi.resume() else remote.playerApi.pause()
    }

    fun skipNext() { appRemote?.playerApi?.skipNext() }

    fun skipPrevious() { appRemote?.playerApi?.skipPrevious() }

    override fun onCleared() {
        appRemote?.let { SpotifyAppRemote.disconnect(it) }
        appRemote = null
        super.onCleared()
    }
}
