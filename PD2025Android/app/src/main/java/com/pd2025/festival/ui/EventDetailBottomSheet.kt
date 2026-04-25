package com.pd2025.festival.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pd2025.festival.R
import com.pd2025.festival.model.Event

class EventDetailBottomSheet : BottomSheetDialogFragment() {

    private var event: Event? = null
    private var onFavToggle: ((String) -> Unit)? = null
    private var onDismissed: (() -> Unit)? = null

    companion object {
        fun newInstance(
            event: Event,
            onFavToggle: (String) -> Unit,
            onDismissed: () -> Unit
        ): EventDetailBottomSheet {
            return EventDetailBottomSheet().apply {
                this.event = event
                this.onFavToggle = onFavToggle
                this.onDismissed = onDismissed
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_event, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ev = event ?: return

        val tvTitle = view.findViewById<TextView>(R.id.tvPopupTitle)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvPopupSubtitle)
        val tvBody = view.findViewById<TextView>(R.id.tvPopupBody)
        val btnFav = view.findViewById<Button>(R.id.btnFavorite)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val webView = view.findViewById<WebView>(R.id.webViewSpotify)

        tvTitle.text = ev.band
        tvSubtitle.text = ev.shortDescription
        tvBody.text = ev.description

        updateFavButton(btnFav, ev.favorite)

        btnFav.setOnClickListener {
            onFavToggle?.invoke(ev.id)
            val updated = event?.copy(favorite = !(event?.favorite ?: false))
            event = updated
            updateFavButton(btnFav, updated?.favorite ?: false)
        }

        btnClose.setOnClickListener {
            onDismissed?.invoke()
            dismiss()
        }

        // Spotify embed player
        if (!ev.spotUrl.isNullOrBlank()) {
            webView.visibility = View.VISIBLE
            setupWebView(webView, ev.spotUrl)
        } else {
            webView.visibility = View.GONE
        }
    }

    private fun updateFavButton(btn: Button, isFav: Boolean) {
        btn.text = if (isFav) "♥" else "♡"
        val color = if (isFav) R.color.spotify_green else R.color.text_secondary
        btn.setTextColor(ContextCompat.getColor(requireContext(), color))
    }

    private fun setupWebView(webView: WebView, artistId: String) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        webView.webViewClient = WebViewClient()
        val url = "https://open.spotify.com/embed/artist/$artistId?utm_source=generator&theme=0"
        webView.loadUrl(url)
    }

    override fun onDestroyView() {
        view?.findViewById<WebView>(R.id.webViewSpotify)?.destroy()
        super.onDestroyView()
    }
}
