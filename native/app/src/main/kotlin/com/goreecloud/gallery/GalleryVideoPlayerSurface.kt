package com.goreecloud.gallery

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import com.goreecloud.gallery.android.AndroidMediaStoreItemUriPolicy

/**
 * Programmatic Android-native video surface for an already-authorized Gallery
 * MediaStore item. The caller remains responsible for viewer UI/lifecycle wiring.
 *
 * This surface accepts only the shared canonical MediaStore item URI boundary;
 * it never interprets file paths, HTTP URLs, or arbitrary ContentProviders.
 */
internal class GalleryVideoPlayerSurface(
    context: Context,
) : FrameLayout(context) {
    private val videoView = VideoView(context)
    private var loadedContentUri: String? = null
    private var loadedPlan: GalleryViewerPlaybackPlan? = null
    private var playbackSession = GalleryVideoPlaybackSession.idle()

    var onPlaybackError: ((what: Int, extra: Int) -> Unit)? = null

    init {
        setBackgroundColor(Color.BLACK)
        videoView.setBackgroundColor(Color.BLACK)
        addView(
            videoView,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        videoView.setOnPreparedListener { mediaPlayer ->
            val plan = loadedPlan ?: return@setOnPreparedListener
            playbackSession = playbackSession.prepared()
            mediaPlayer.isLooping = plan.shouldLoop
            if (playbackSession.wantsPlayback()) videoView.start()
        }
        videoView.setOnCompletionListener {
            playbackSession = playbackSession.completed()
        }
        videoView.setOnErrorListener { _, what, extra ->
            playbackSession = playbackSession.failed()
            onPlaybackError?.invoke(what, extra)
            true
        }
    }

    fun load(contentUri: String, plan: GalleryViewerPlaybackPlan) {
        require(plan.presentation == GalleryViewerPresentation.VIDEO_PLAYBACK) {
            "GalleryVideoPlayerSurface requires a video playback plan"
        }
        val canonicalUri = AndroidMediaStoreItemUriPolicy.requireCanonicalItemUri(contentUri)
        loadedContentUri = canonicalUri
        loadedPlan = plan
        playbackSession = GalleryVideoPlaybackSession.loading(plan)
        videoView.setVideoURI(Uri.parse(canonicalUri))
        videoView.requestFocus()
    }

    fun play(): Boolean {
        if (loadedContentUri == null) return false
        playbackSession = playbackSession.play()
        if (!playbackSession.wantsPlayback()) return false
        videoView.start()
        return true
    }

    fun pause(): Boolean {
        if (loadedContentUri == null) return false
        val wasRequested = playbackSession.wantsPlayback() || videoView.isPlaying
        playbackSession = playbackSession.pause()
        if (wasRequested) videoView.pause()
        return wasRequested
    }

    fun pauseForHost(): Boolean {
        if (loadedContentUri == null) return false
        val wantedPlayback = playbackSession.wantsPlayback()
        playbackSession = playbackSession.pauseForHost()
        val paused = wantedPlayback && !playbackSession.wantsPlayback()
        if (paused) videoView.pause()
        return paused
    }

    fun resumeForHost(): Boolean {
        if (loadedContentUri == null) return false
        val wantedPlayback = playbackSession.wantsPlayback()
        playbackSession = playbackSession.resumeForHost()
        val resumed = !wantedPlayback && playbackSession.wantsPlayback()
        if (resumed) videoView.start()
        return resumed
    }

    fun stop() {
        if (loadedContentUri != null) videoView.stopPlayback()
        loadedContentUri = null
        loadedPlan = null
        playbackSession = playbackSession.stopped()
    }

    fun hasLoadedVideo(): Boolean =
        loadedContentUri != null && playbackSession.state != GalleryVideoPlaybackState.IDLE

    fun isPlaying(): Boolean =
        playbackSession.state == GalleryVideoPlaybackState.PLAYING && videoView.isPlaying

    fun playbackState(): GalleryVideoPlaybackState = playbackSession.state
}
