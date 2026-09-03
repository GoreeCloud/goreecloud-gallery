package com.goreecloud.gallery.android

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidMediaMutationRequestsTest {
    @Test
    fun `scoped mutation requests require Android 11 or newer`() {
        assertFalse(AndroidMediaMutationRequests.isSupported(29))
        assertTrue(AndroidMediaMutationRequests.isSupported(30))
        assertTrue(AndroidMediaMutationRequests.isSupported(36))
    }

    @Test
    fun `normalization keeps bounded unique MediaStore content uris`() {
        val first = "content://media/external/images/media/42"
        val second = "content://media/external/video/media/7"

        assertEquals(
            listOf(first, second),
            AndroidMediaMutationRequests.normalizeMediaStoreUris(
                listOf("  $first  ", second, first),
            ),
        )
    }

    @Test
    fun `normalization rejects non MediaStore authorities`() {
        assertFailsWith<IllegalArgumentException> {
            AndroidMediaMutationRequests.normalizeMediaStoreUris(
                listOf("content://example.provider/photos/1"),
            )
        }
    }

    @Test
    fun `normalization rejects file and network uris`() {
        listOf(
            "file:///storage/emulated/0/DCIM/photo.jpg",
            "https://example.invalid/photo.jpg",
        ).forEach { uri ->
            assertFailsWith<IllegalArgumentException> {
                AndroidMediaMutationRequests.normalizeMediaStoreUris(listOf(uri))
            }
        }
    }

    @Test
    fun `normalization enforces the mutation item bound`() {
        val uris = (1..AndroidMediaMutationRequests.MAX_MUTATION_ITEMS + 1)
            .map { "content://media/external/images/media/$it" }

        assertFailsWith<IllegalArgumentException> {
            AndroidMediaMutationRequests.normalizeMediaStoreUris(uris)
        }
    }
}
