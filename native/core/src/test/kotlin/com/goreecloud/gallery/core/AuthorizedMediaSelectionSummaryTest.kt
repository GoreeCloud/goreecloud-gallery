package com.goreecloud.gallery.core

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthorizedMediaSelectionSummaryTest {
    private val now = Instant.parse("2026-08-31T12:00:00Z")

    private fun media(
        id: String,
        mimeType: String = "image/jpeg",
        sizeBytes: Long = 10,
        uriSuffix: String = id,
    ) = MediaItem(
        id = id,
        contentUri = "content://media/$uriSuffix",
        displayName = "item-$id",
        mimeType = mimeType,
        capturedAt = now,
        modifiedAt = now,
        width = 100,
        height = 100,
        durationMillis = if (mimeType.startsWith("video/")) 1_000 else null,
        sizeBytes = sizeBytes,
    )

    @Test
    fun summaryCountsOnlySelectedPresentedAuthorizedItems() {
        val one = media("1", sizeBytes = 12)
        val two = media("2", mimeType = "video/mp4", sizeBytes = 30)
        val three = media("3", sizeBytes = 50)
        val selection = AuthorizedMediaSelection(listOf(one, two, three))
        selection.select("1")
        selection.select("2")
        selection.select("3")

        val summary = selection.selectionSummary(listOf(two, one))

        assertEquals(2, summary.selectedCount)
        assertEquals(1, summary.imageCount)
        assertEquals(1, summary.videoCount)
        assertEquals(42, summary.totalSizeBytes)
    }

    @Test
    fun mismatchedAuthorizedUriIsExcludedFromSummary() {
        val one = media("1", sizeBytes = 12)
        val selection = AuthorizedMediaSelection(listOf(one))
        selection.select("1")

        val summary = selection.selectionSummary(listOf(media("1", uriSuffix = "different")))

        assertEquals(0, summary.selectedCount)
        assertEquals(0, summary.totalSizeBytes)
    }

    @Test
    fun refreshedAuthorizationPrunesSummaryImmediately() {
        val one = media("1", sizeBytes = 12)
        val two = media("2", sizeBytes = 20)
        val selection = AuthorizedMediaSelection(listOf(one, two))
        selection.select("1")
        selection.select("2")
        selection.replaceAuthorizedSnapshot(listOf(two))

        assertEquals(20, selection.selectionSummary(listOf(one, two)).totalSizeBytes)
    }

    @Test
    fun totalSizeOverflowFailsInsteadOfWrapping() {
        val one = media("1", sizeBytes = Long.MAX_VALUE)
        val two = media("2", sizeBytes = 1)
        val selection = AuthorizedMediaSelection(listOf(one, two))
        selection.select("1")
        selection.select("2")

        assertFailsWith<ArithmeticException> {
            selection.selectionSummary(listOf(one, two))
        }
    }
}
