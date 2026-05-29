package sk.punkacidetom.pd2026.feature.spotify.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

object SpotifyLauncher {

    private const val SPOTIFY_PACKAGE = "com.spotify.music"

    fun isSpotifyInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(SPOTIFY_PACKAGE, 0)
        true
    } catch (e: Exception) {
        false
    }

    fun openPlaylist(context: Context, playlistId: String) {
        openUri(context,
            spotifyUri = "spotify:playlist:$playlistId",
            webUrl = "https://open.spotify.com/playlist/$playlistId"
        )
    }

    fun openArtist(context: Context, artistId: String) {
        if (artistId.isBlank()) return
        openUri(context,
            spotifyUri = "spotify:artist:$artistId",
            webUrl = "https://open.spotify.com/artist/$artistId"
        )
    }

    private fun openUri(context: Context, spotifyUri: String, webUrl: String) {
        if (isSpotifyInstalled(context)) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUri))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // Fall through to web URL
            }
        }
        // Fallback: Chrome Custom Tabs
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(context, Uri.parse(webUrl))
    }
}
