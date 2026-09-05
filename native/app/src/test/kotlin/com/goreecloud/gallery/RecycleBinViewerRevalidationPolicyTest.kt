package com.goreecloud.gallery

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecycleBinViewerRevalidationPolicyTest {
    @Test
    fun unchangedAuthoritativeSnapshotKeepsViewer() {
        assertFalse(
            RecycleBinViewerRevalidationPolicy.requiresViewerReset(
                previousUris = listOf("content://media/a", "content://media/b"),
                currentUris = listOf("content://media/a", "content://media/b"),
            ),
        )
    }

    @Test
    fun removedItemRequiresViewerReset() {
        assertTrue(
            RecycleBinViewerRevalidationPolicy.requiresViewerReset(
                previousUris = listOf("content://media/a", "content://media/b"),
                currentUris = listOf("content://media/a"),
            ),
        )
    }

    @Test
    fun addedOrReorderedAuthoritativeItemsRequireReset() {
        assertTrue(
            RecycleBinViewerRevalidationPolicy.requiresViewerReset(
                previousUris = listOf("content://media/a", "content://media/b"),
                currentUris = listOf("content://media/b", "content://media/a", "content://media/c"),
            ),
        )
    }
}
