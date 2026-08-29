package com.goreecloud.gallery

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GalleryMediaAccessPolicyTest {
    @Test
    fun android14SelectedAccessIsPartialRatherThanFull() {
        val scope = GalleryMediaAccessPolicy.resolve(
            GalleryMediaPermissionSnapshot(
                apiLevel = 34,
                readMediaVisualUserSelected = true,
            ),
        )

        assertEquals(GalleryMediaAccessScope.SELECTED, scope)
        assertTrue(GalleryMediaAccessPolicy.canRead(scope))
        assertTrue(GalleryMediaAccessPolicy.isPartial(scope))
    }

    @Test
    fun fullTypeGrantsTakePrecedenceOverSelectedMarker() {
        assertEquals(
            GalleryMediaAccessScope.IMAGES,
            GalleryMediaAccessPolicy.resolve(
                GalleryMediaPermissionSnapshot(
                    apiLevel = 36,
                    readMediaImages = true,
                    readMediaVisualUserSelected = true,
                ),
            ),
        )
        assertEquals(
            GalleryMediaAccessScope.IMAGES_AND_VIDEOS,
            GalleryMediaAccessPolicy.resolve(
                GalleryMediaPermissionSnapshot(
                    apiLevel = 36,
                    readMediaImages = true,
                    readMediaVideo = true,
                    readMediaVisualUserSelected = true,
                ),
            ),
        )
    }

    @Test
    fun android13TracksImageAndVideoAuthoritySeparately() {
        assertEquals(
            GalleryMediaAccessScope.IMAGES,
            GalleryMediaAccessPolicy.resolve(
                GalleryMediaPermissionSnapshot(apiLevel = 33, readMediaImages = true),
            ),
        )
        assertEquals(
            GalleryMediaAccessScope.VIDEOS,
            GalleryMediaAccessPolicy.resolve(
                GalleryMediaPermissionSnapshot(apiLevel = 33, readMediaVideo = true),
            ),
        )
    }

    @Test
    fun legacyReadExternalStorageIsFullOnlyThroughApi32() {
        assertEquals(
            GalleryMediaAccessScope.LEGACY_FULL,
            GalleryMediaAccessPolicy.resolve(
                GalleryMediaPermissionSnapshot(apiLevel = 32, readExternalStorage = true),
            ),
        )
        assertEquals(
            GalleryMediaAccessScope.DENIED,
            GalleryMediaAccessPolicy.resolve(
                GalleryMediaPermissionSnapshot(apiLevel = 33, readExternalStorage = true),
            ),
        )
    }

    @Test
    fun missingAuthorityFailsClosed() {
        val scope = GalleryMediaAccessPolicy.resolve(GalleryMediaPermissionSnapshot(apiLevel = 36))
        assertEquals(GalleryMediaAccessScope.DENIED, scope)
        assertFalse(GalleryMediaAccessPolicy.canRead(scope))
        assertFalse(GalleryMediaAccessPolicy.isPartial(scope))
    }
}
