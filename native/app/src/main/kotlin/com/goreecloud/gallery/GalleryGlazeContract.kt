package com.goreecloud.gallery

/**
 * Repository-local GLAZE UI 1.0.0 source contract consumed by the first-party native Gallery shell.
 *
 * This VERSION describes the source that is actually mapped by the current native implementation;
 * it is not the current GoreeCloud consumer requirement. The live platform requirement is GLAZE UI
 * V1.3 / 1.3.0 Adaptive Resonance. Gallery therefore remains migration-required until its native
 * source is deliberately migrated to the current Stable line and application-specific rendered,
 * accessibility, adaptive, representative-device, rollback, and release acceptance is repeated.
 *
 * Do not change VERSION merely to match the required target. It must move only with the reviewed
 * source migration that implements and validates the corresponding Gallery contract.
 */
object GalleryGlazeContract {
    const val VERSION = "1.0.0"
    const val GENERAL_TARGET_DP = 48
    const val MAX_RENDERED_MEDIA_ROWS = 100
    const val MIN_GRID_TILE_DP = 78
    const val MIN_ALBUM_TILE_DP = 132
    const val NAVIGATION_HEIGHT_DP = 54
    const val NAVIGATION_RADIUS_DP = 26
    const val NAVIGATION_SIDE_MARGIN_DP = 24
    const val NAVIGATION_BOTTOM_MARGIN_DP = 10
    const val NAVIGATION_ELEVATION_DP = 4
    const val NAVIGATION_RESERVED_SPACE_DP = 76
    const val CONTENT_BOTTOM_INSET_DP = 28

    fun horizontalGutterDp(widthDp: Int): Int = when {
        widthDp >= 1200 -> 40
        widthDp >= 840 -> 32
        widthDp >= 600 -> 24
        else -> 16
    }

    fun gridColumns(widthDp: Int): Int = when {
        widthDp >= 1200 -> 7
        widthDp >= 840 -> 6
        widthDp >= 600 -> 5
        widthDp >= 360 -> 4
        else -> 3
    }

    fun albumGridColumns(widthDp: Int): Int = when {
        widthDp >= 1200 -> 5
        widthDp >= 840 -> 4
        widthDp >= 600 -> 3
        else -> 2
    }
}
