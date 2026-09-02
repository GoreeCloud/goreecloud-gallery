package com.goreecloud.gallery

/**
 * Repository-local Glaze UI 2.2 Stable source contract consumed by the first-party native
 * Gallery shell. This is an Adoption Candidate mapping, not a complete conformance claim.
 * Rendered accessibility, Human Visual Excellence, and representative-device acceptance
 * remain separate release gates.
 */
object GalleryGlazeContract {
    const val VERSION = "2.2.0"
    const val STABLE_PROMOTION_HEAD = "fb5ecde4a8258503789ffde08ac46a2e524ef71e"
    const val STABLE_RELEASE_REVISION = "6731098b28dd0393faa878c70d989a221d714a20"

    const val GENERAL_TARGET_DP = 48
    const val TOUCH_ASSISTANCE_TARGET_DP = 56

    const val SYSTEM_GLAZE_DOMINANT_PANEL_MAX = 1
    const val SYSTEM_GLAZE_SMALL_FLOATING_CONTROLS_MAX = 3
    const val NESTED_BACKDROP_BLUR_ALLOWED = false

    // Native mapping of the Glaze UI 2.2 GlzToast standard variant.
    const val TRANSIENT_NOTICE_STANDARD_HEIGHT_DP = 52
    const val TRANSIENT_NOTICE_RADIUS_DP = 20
    const val TRANSIENT_NOTICE_HORIZONTAL_PADDING_DP = 16
    const val TRANSIENT_NOTICE_TEXT_SP = 14f

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

    fun interactionTargetDp(touchAssistance: Boolean): Int =
        if (touchAssistance) TOUCH_ASSISTANCE_TARGET_DP else GENERAL_TARGET_DP

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
