package com.goreecloud.gallery

/**
 * Repository-local GLAZE UI V1.3 source mapping for the first-party native Gallery shell.
 *
 * The current GoreeCloud design-system authority is GLAZE UI V1.3 / 1.3.0 — Adaptive Resonance.
 * This source mapping is pinned to the exact authoritative Glaze repository revision below and uses
 * the semantic V1.3 shape/layout/motion roles together with their governed inherited V1.2 numeric
 * baselines. The `.candidate` token filenames are implementation-provenance artifacts in the Glaze
 * repository; consumer eligibility comes from the promoted V1.3 authority, not from those filenames.
 *
 * VERSION records the source contract mapped by this native implementation. It does not establish
 * Gallery application conformance, rendered acceptance, production eligibility, Release Candidate,
 * or Stable status. Gallery remains fail-closed on those claims until its separate application gates
 * are completed and accepted.
 */
object GalleryGlazeContract {
    const val VERSION = "1.3.0"
    const val AUTHORITY_REVISION = "8354308445da9ac35ced2b37a7f503a08a0aaf72"

    // GLAZE UI V1.3 layout.space.scale -> governed V1.2 spatial baseline.
    const val SPACE_MICRO_DP = 2
    const val SPACE_HAIRLINE_DP = 4
    const val SPACE_CONTROL_DP = 8
    const val SPACE_COMPACT_CLUSTER_DP = 12
    const val SPACE_STANDARD_CLUSTER_DP = 16
    const val SPACE_CONTENT_DP = 24
    const val SPACE_SECTION_DP = 32
    const val SPACE_REGION_DP = 48

    // GLAZE UI V1.3 semantic shape roles -> governed V1.2 geometry baseline.
    const val SHAPE_QUIET_DP = 10
    const val SHAPE_CONTROL_DP = 12
    const val SHAPE_CONTAINER_DP = 20
    const val SHAPE_ROUNDED_DP = 24
    const val SHAPE_OVERLAY_DP = 28
    const val SHAPE_CAPSULE_DP = 999

    // GLAZE UI V1.3 semantic motion roles -> governed V1.2 motion baseline.
    const val MOTION_MICRO_MS = 160L
    const val MOTION_STANDARD_MS = 240L
    const val MOTION_CONNECTED_MS = 360L
    const val MOTION_SPATIAL_MS = 480L
    const val MOTION_REDUCED_STANDARD_MS = 180L
    const val MOTION_MINIMAL_MS = 0L

    // Current Android acceptance floor inherited by the V1.3 form-factor contract.
    const val GENERAL_TARGET_DP = 48

    const val MAX_RENDERED_MEDIA_ROWS = 100
    const val MIN_GRID_TILE_DP = 78
    const val MIN_ALBUM_TILE_DP = 132

    // Gallery-specific bottom navigation composition mapped onto semantic V1.3 roles.
    const val NAVIGATION_HEIGHT_DP = 54
    const val NAVIGATION_RADIUS_DP = SHAPE_CAPSULE_DP
    const val NAVIGATION_SIDE_MARGIN_DP = SPACE_CONTENT_DP
    const val NAVIGATION_BOTTOM_MARGIN_DP = SPACE_COMPACT_CLUSTER_DP
    const val NAVIGATION_ELEVATION_DP = 4
    const val NAVIGATION_RESERVED_SPACE_DP = 78
    const val CONTENT_BOTTOM_INSET_DP = SPACE_SECTION_DP

    /**
     * Android Gallery capability adapter for current composition widths.
     *
     * The returned gutters are governed Glaze spatial values. The width thresholds are local Android
     * composition heuristics, not canonical Glaze device identities or universal breakpoints.
     */
    fun horizontalGutterDp(widthDp: Int): Int = when {
        widthDp >= 1200 -> SPACE_REGION_DP
        widthDp >= 840 -> SPACE_SECTION_DP
        widthDp >= 600 -> SPACE_CONTENT_DP
        else -> SPACE_STANDARD_CLUSTER_DP
    }

    /** Product-specific media density, not the Glaze foundational layout-grid column token. */
    fun gridColumns(widthDp: Int): Int = when {
        widthDp >= 1200 -> 7
        widthDp >= 840 -> 6
        widthDp >= 600 -> 5
        widthDp >= 360 -> 4
        else -> 3
    }

    /** Product-specific album-card density, not the Glaze foundational layout-grid column token. */
    fun albumGridColumns(widthDp: Int): Int = when {
        widthDp >= 1200 -> 5
        widthDp >= 840 -> 4
        widthDp >= 600 -> 3
        else -> 2
    }
}
