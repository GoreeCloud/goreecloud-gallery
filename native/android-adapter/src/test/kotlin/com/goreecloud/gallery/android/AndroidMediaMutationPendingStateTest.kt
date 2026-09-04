package com.goreecloud.gallery.android

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AndroidMediaMutationPendingStateTest {
    @Test
    fun `capture freezes bounded canonical unique item uris`() {
        val first = "content://media/external/images/media/42"
        val second = "content://media/external/video/media/7"

        val state = AndroidMediaMutationPendingStates.capture(
            AndroidMediaMutationMode.DELETE,
            listOf("  $first  ", second, first),
        )

        assertEquals(AndroidMediaMutationMode.DELETE, state.mode)
        assertEquals(listOf(first, second), state.contentUris)
        assertEquals("DELETE", AndroidMediaMutationPendingStates.modeName(state))
        assertEquals(listOf(first, second), AndroidMediaMutationPendingStates.contentUriValues(state).toList())
    }

    @Test
    fun `restore reconstructs exact previously validated mutation state`() {
        val uri = "content://media/external_primary/images/media/99"

        val state = AndroidMediaMutationPendingStates.restore("RESTORE", listOf(uri))

        assertEquals(AndroidMediaMutationMode.RESTORE, state?.mode)
        assertEquals(listOf(uri), state?.contentUris)
    }

    @Test
    fun `restore fails closed for missing or unknown state`() {
        val uri = "content://media/external/images/media/42"

        assertNull(AndroidMediaMutationPendingStates.restore(null, listOf(uri)))
        assertNull(AndroidMediaMutationPendingStates.restore("", listOf(uri)))
        assertNull(AndroidMediaMutationPendingStates.restore("NOT_A_MODE", listOf(uri)))
        assertNull(AndroidMediaMutationPendingStates.restore("DELETE", null))
        assertNull(AndroidMediaMutationPendingStates.restore("DELETE", emptyList()))
    }

    @Test
    fun `restore fails closed for broad malformed or ambiguous uris`() {
        listOf(
            "content://media/external/images/media",
            "content://media/external/images/media/0",
            "content://media/external/images/media/42?include_pending=1",
            "content://example.provider/images/media/42",
            "file:///storage/emulated/0/DCIM/photo.jpg",
        ).forEach { uri ->
            assertNull(AndroidMediaMutationPendingStates.restore("DELETE", listOf(uri)))
        }
    }

    @Test
    fun `restore fails closed above the mutation item bound`() {
        val uris = (1..AndroidMediaMutationRequests.MAX_MUTATION_ITEMS + 1)
            .map { "content://media/external/images/media/$it" }

        assertNull(AndroidMediaMutationPendingStates.restore("RESTORE", uris))
    }
}
