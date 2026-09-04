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
    fun `normalization accepts media specific item uris on alternate volumes`() {
        val image = "content://media/external_primary/images/media/42"
        val video = "content://media/1234-5678/video/media/7"

        assertEquals(
            listOf(image, video),
            AndroidMediaMutationRequests.normalizeMediaStoreUris(listOf(image, video)),
        )
    }

    @Test
    fun `normalization rejects generic files collection item uris`() {
        assertFailsWith<IllegalArgumentException> {
            AndroidMediaMutationRequests.normalizeMediaStoreUris(
                listOf("content://media/external/file/42"),
            )
        }
    }

    @Test
    fun `normalization rejects media collection uris without item id`() {
        listOf(
            "content://media/external/images/media",
            "content://media/external/video/media",
        ).forEach { uri ->
            assertFailsWith<IllegalArgumentException> {
                AndroidMediaMutationRequests.normalizeMediaStoreUris(listOf(uri))
            }
        }
    }

    @Test
    fun `normalization rejects non positive and non numeric media item ids`() {
        listOf(
            "content://media/external/images/media/0",
            "content://media/external/images/media/-1",
            "content://media/external/images/media/not-an-id",
        ).forEach { uri ->
            assertFailsWith<IllegalArgumentException> {
                AndroidMediaMutationRequests.normalizeMediaStoreUris(listOf(uri))
            }
        }
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
    fun `normalization rejects non canonical MediaStore item uris`() {
        listOf(
            "content://media/external/images/media/42?include_pending=1",
            "content://media/external/images/media/42#fragment",
            "content://user@media/external/images/media/42",
            "content://media:80/external/images/media/42",
        ).forEach { uri ->
            assertFailsWith<IllegalArgumentException> {
                AndroidMediaMutationRequests.normalizeMediaStoreUris(listOf(uri))
            }
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
