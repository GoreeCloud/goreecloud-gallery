package com.goreecloud.gallery.core

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GallerySelectionPolicyTest {
    @Test
    fun `toggle only admits items from current scope`() {
        val first = media("1")
        val second = media("2")
        val outside = media("3")

        val selected = GallerySelectionPolicy.toggle(emptySet(), outside, listOf(first, second))

        assertTrue(selected.isEmpty())
    }

    @Test
    fun `toggle adds and removes an authorized item`() {
        val first = media("1")
        val scope = listOf(first)

        val selected = GallerySelectionPolicy.toggle(emptySet(), first, scope)
        assertEquals(setOf(first.contentUri), selected)

        val cleared = GallerySelectionPolicy.toggle(selected, first, scope)
        assertTrue(cleared.isEmpty())
    }

    @Test
    fun `prune removes stale selection when presentation scope changes`() {
        val first = media("1")
        val second = media("2")

        val pruned = GallerySelectionPolicy.prune(
            setOf(first.contentUri, second.contentUri, "content://media/external/file/999"),
            listOf(second),
        )

        assertEquals(setOf(second.contentUri), pruned)
    }

    @Test
    fun `resolve preserves current presentation order and ignores foreign uris`() {
        val first = media("1")
        val second = media("2")
        val third = media("3")
        val scope = listOf(third, first, second)

        val resolved = GallerySelectionPolicy.resolve(
            scope,
            setOf(first.contentUri, third.contentUri, "content://foreign/not-authorized"),
        )

        assertEquals(listOf(third, first), resolved)
    }

    @Test
    fun `explicit resolution returns current item values for exact requested uris`() {
        val stale = media("1").copy(displayName = "old-name.jpg")
        val current = media("1").copy(displayName = "current-name.jpg")
        val second = media("2")

        assertEquals(
            listOf(current, second),
            GallerySelectionPolicy.resolveExactCurrentScope(
                currentScope = listOf(current, second),
                requestedItems = listOf(stale, second),
            ),
        )
    }

    @Test
    fun `explicit resolution fails closed when requested uri left current scope`() {
        val first = media("1")
        val stale = media("2")

        assertNull(
            GallerySelectionPolicy.resolveExactCurrentScope(
                currentScope = listOf(first),
                requestedItems = listOf(stale),
            ),
        )
    }

    @Test
    fun `explicit resolution rejects duplicate or ambiguous current uris`() {
        val first = media("1")

        assertNull(
            GallerySelectionPolicy.resolveExactCurrentScope(
                currentScope = listOf(first),
                requestedItems = listOf(first, first),
            ),
        )
        assertNull(
            GallerySelectionPolicy.resolveExactCurrentScope(
                currentScope = listOf(first, first.copy(displayName = "duplicate.jpg")),
                requestedItems = listOf(first),
            ),
        )
    }

    @Test
    fun `select all is bounded to the supplied current scope`() {
        val first = media("1")
        val second = media("2")

        assertEquals(
            linkedSetOf(first.contentUri, second.contentUri),
            GallerySelectionPolicy.selectAll(listOf(first, second)),
        )
    }

    private fun media(id: String): MediaItem = MediaItem(
        id = id,
        contentUri = "content://media/external/file/$id",
        displayName = "item-$id.jpg",
        mimeType = "image/jpeg",
        capturedAt = Instant.parse("2026-08-31T12:00:00Z"),
        modifiedAt = Instant.parse("2026-08-31T12:00:00Z"),
        width = 1080,
        height = 1920,
        durationMillis = null,
        sizeBytes = 1024,
        albumId = "camera",
        albumName = "Camera",
    )
}
