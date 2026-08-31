package com.goreecloud.gallery.core

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class AuthorizedMediaSelectionTest {
    private val now = Instant.parse("2026-08-31T12:00:00Z")

    private fun media(id: String, uriSuffix: String = id) = MediaItem(
        id = id,
        contentUri = "content://media/$uriSuffix",
        displayName = "photo-$id.jpg",
        mimeType = "image/jpeg",
        capturedAt = now,
        modifiedAt = now,
        width = 100,
        height = 100,
        durationMillis = null,
        sizeBytes = 10,
    )

    @Test
    fun selectionFailsClosedOutsideAuthorizedSnapshot() {
        val selection = AuthorizedMediaSelection(listOf(media("1"), media("2")))

        assertNull(selection.toggle("missing"))
        assertFalse(selection.select("missing"))
        assertFalse(selection.deselect("missing"))
        assertTrue(selection.isEmpty)
    }

    @Test
    fun selectedItemsFollowCurrentAuthorizedSnapshotOrder() {
        val selection = AuthorizedMediaSelection(listOf(media("1"), media("2"), media("3")))
        assertTrue(selection.select("3"))
        assertTrue(selection.select("1"))

        assertEquals(listOf("1", "3"), selection.selectedItems().map { it.id })
        assertEquals(2, selection.size)
    }

    @Test
    fun refreshedAuthorizationImmediatelyPrunesStaleSelection() {
        val selection = AuthorizedMediaSelection(listOf(media("1"), media("2")))
        selection.select("1")
        selection.select("2")

        selection.replaceAuthorizedSnapshot(listOf(media("2"), media("3")))

        assertFalse(selection.isSelected("1"))
        assertTrue(selection.isSelected("2"))
        assertEquals(listOf("2"), selection.selectedItems().map { it.id })
    }

    @Test
    fun presentedSelectionRequiresMatchingAuthorizedIdentityAndUri() {
        val one = media("1")
        val two = media("2")
        val selection = AuthorizedMediaSelection(listOf(one, two))
        selection.select("1")
        selection.select("2")

        val mismatchedUri = media("1", uriSuffix = "different")
        assertEquals(
            listOf("2"),
            selection.selectedPresentedItems(listOf(mismatchedUri, two)).map { it.id },
        )
    }

    @Test
    fun duplicateAuthorizedIdsAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            AuthorizedMediaSelection(listOf(media("1"), media("1", uriSuffix = "other")))
        }
    }

    @Test
    fun toggleAndClearAreDeterministic() {
        val selection = AuthorizedMediaSelection(listOf(media("1")))
        assertEquals(true, selection.toggle("1"))
        assertTrue(selection.isSelected("1"))
        assertEquals(false, selection.toggle("1"))
        assertFalse(selection.isSelected("1"))
        selection.select("1")
        selection.clear()
        assertTrue(selection.isEmpty)
    }
}
