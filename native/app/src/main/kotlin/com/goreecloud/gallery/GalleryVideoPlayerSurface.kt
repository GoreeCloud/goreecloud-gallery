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
    private var resumeAfterHostPause = false

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
            mediaPlayer.isLooping = plan.shouldLoop
            if (plan.shouldAutoPlay) videoView.start()
        }
        videoView.setOnErrorListener { _, what, extra ->
            resumeAfterHostPause = false
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
        resumeAfterHostPause = false
        videoView.setVideoURI(Uri.parse(canonicalUri))
        videoView.requestFocus()
    }

    fun play(): Boolean {
        if (loadedContentUri == null) return false
        videoView.start()
        return true
    }

    fun pause(): Boolean {
        if (loadedContentUri == null) return false
        val wasPlaying = videoView.isPlaying
        videoView.pause()
        return wasPlaying
    }

    fun pauseForHost(): Boolean {
        if (loadedContentUri == null) {
            resumeAfterHostPause = false
            return false
        }
        resumeAfterHostPause = videoView.isPlaying
        if (resumeAfterHostPause) videoView.pause()
        return resumeAfterHostPause
    }

    fun resumeForHost(): Boolean {
        if (!resumeAfterHostPause || loadedContentUri == null) return false
        resumeAfterHostPause = false
        videoView.start()
        return true
    }

    fun stop() {
        if (loadedContentUri != null) videoView.stopPlayback()
        loadedContentUri = null
        loadedPlan = null
        resumeAfterHostPause = false
    }

    fun hasLoadedVideo(): Boolean = loadedContentUri != null

    fun isPlaying(): Boolean = loadedContentUri != null && videoView.isPlaying
}
