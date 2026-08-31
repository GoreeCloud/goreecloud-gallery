package com.goreecloud.gallery

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GalleryGlazeContractTest {
    @Test
    fun `native shell targets current stable Glaze contract and accessible target floor`() {
        assertEquals("2.1.0", GalleryGlazeContract.VERSION)
        assertTrue(GalleryGlazeContract.GENERAL_TARGET_DP >= 48)
    }

    @Test
    fun `adaptive gutters expand without shrinking phone composition`() {
        assertEquals(16, GalleryGlazeContract.horizontalGutterDp(390))
        assertEquals(24, GalleryGlazeContract.horizontalGutterDp(820))
        assertEquals(32, GalleryGlazeContract.horizontalGutterDp(900))
        assertEquals(40, GalleryGlazeContract.horizontalGutterDp(1280))
    }

    @Test
    fun `photo grid adapts across representative width classes`() {
        assertEquals(3, GalleryGlazeContract.gridColumns(390))
        assertEquals(5, GalleryGlazeContract.gridColumns(820))
        assertEquals(6, GalleryGlazeContract.gridColumns(900))
        assertEquals(7, GalleryGlazeContract.gridColumns(1280))
        assertTrue(GalleryGlazeContract.MIN_GRID_TILE_DP >= 92)
    }

    @Test
    fun `rendered local library stays bounded`() {
        assertEquals(100, GalleryGlazeContract.MAX_RENDERED_MEDIA_ROWS)
    }
}
