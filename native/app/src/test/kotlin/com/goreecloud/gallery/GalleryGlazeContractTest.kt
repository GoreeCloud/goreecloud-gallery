package com.goreecloud.gallery

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GalleryGlazeContractTest {
    @Test
    fun `native shell targets current official Glaze contract and accessible target floor`() {
        assertEquals("1.1.0", GalleryGlazeContract.VERSION)
        assertTrue(GalleryGlazeContract.GENERAL_TARGET_DP >= 48)
        assertTrue(GalleryGlazeContract.NAVIGATION_HEIGHT_DP >= GalleryGlazeContract.GENERAL_TARGET_DP)
    }

    @Test
    fun `adaptive gutters expand without shrinking phone composition`() {
        assertEquals(16, GalleryGlazeContract.horizontalGutterDp(390))
        assertEquals(24, GalleryGlazeContract.horizontalGutterDp(820))
        assertEquals(32, GalleryGlazeContract.horizontalGutterDp(900))
        assertEquals(40, GalleryGlazeContract.horizontalGutterDp(1280))
    }

    @Test
    fun `photo grid is media dense on phones and adapts across wider classes`() {
        assertEquals(3, GalleryGlazeContract.gridColumns(320))
        assertEquals(4, GalleryGlazeContract.gridColumns(360))
        assertEquals(4, GalleryGlazeContract.gridColumns(390))
        assertEquals(5, GalleryGlazeContract.gridColumns(820))
        assertEquals(6, GalleryGlazeContract.gridColumns(900))
        assertEquals(7, GalleryGlazeContract.gridColumns(1280))
        assertTrue(GalleryGlazeContract.MIN_GRID_TILE_DP >= 78)
    }

    @Test
    fun `album grid stays more spacious than the media timeline`() {
        assertEquals(2, GalleryGlazeContract.albumGridColumns(390))
        assertEquals(3, GalleryGlazeContract.albumGridColumns(820))
        assertEquals(4, GalleryGlazeContract.albumGridColumns(900))
        assertEquals(5, GalleryGlazeContract.albumGridColumns(1280))
        assertTrue(GalleryGlazeContract.MIN_ALBUM_TILE_DP > GalleryGlazeContract.MIN_GRID_TILE_DP)
    }

    @Test
    fun `navigation capsule reserves a dedicated phone action zone`() {
        assertEquals(54, GalleryGlazeContract.NAVIGATION_HEIGHT_DP)
        assertEquals(26, GalleryGlazeContract.NAVIGATION_RADIUS_DP)
        assertEquals(24, GalleryGlazeContract.NAVIGATION_SIDE_MARGIN_DP)
        assertEquals(10, GalleryGlazeContract.NAVIGATION_BOTTOM_MARGIN_DP)
        assertEquals(4, GalleryGlazeContract.NAVIGATION_ELEVATION_DP)
        assertTrue(
            GalleryGlazeContract.NAVIGATION_RESERVED_SPACE_DP >=
                GalleryGlazeContract.NAVIGATION_HEIGHT_DP + GalleryGlazeContract.NAVIGATION_BOTTOM_MARGIN_DP,
        )
        assertTrue(GalleryGlazeContract.CONTENT_BOTTOM_INSET_DP < GalleryGlazeContract.NAVIGATION_RESERVED_SPACE_DP)
    }

    @Test
    fun `rendered local library stays bounded`() {
        assertEquals(100, GalleryGlazeContract.MAX_RENDERED_MEDIA_ROWS)
    }
}
