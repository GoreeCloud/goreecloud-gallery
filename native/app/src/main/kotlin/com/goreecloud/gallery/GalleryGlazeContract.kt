package com.goreecloud.gallery

/**
 * Repository-local Glaze UI 2.0 source contract consumed by the first-party native Gallery shell.
 * Rendered accessibility and representative-device acceptance remain separate release gates.
 */
object GalleryGlazeContract {
    const val VERSION = "2.0.0"
    const val GENERAL_TARGET_DP = 48
    const val MAX_RENDERED_MEDIA_ROWS = 100

    fun horizontalGutterDp(widthDp: Int): Int = when {
        widthDp >= 1200 -> 40
        widthDp >= 840 -> 32
        widthDp >= 600 -> 24
        else -> 16
    }
}
