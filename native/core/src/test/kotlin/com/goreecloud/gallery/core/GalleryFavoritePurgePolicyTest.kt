package com.goreecloud.gallery.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GalleryFavoritePurgePolicyTest {
    @Test
    fun removesOnlyPermanentlyDeletedFavoriteUris() {
        val result = GalleryFavoritePurgePolicy.removePurged(
            currentFavorites = listOf("content://media/1", "content://media/2", "content://media/3"),
            permanentlyDeletedUris = listOf("content://media/2", "content://media/9"),
        )

        assertTrue(result.changed)
        assertEquals(
            linkedSetOf("content://media/1", "content://media/3"),
            result.favorites,
        )
    }

    @Test
    fun unchangedWhenPurgeDoesNotIntersectFavorites() {
        val result = GalleryFavoritePurgePolicy.removePurged(
            currentFavorites = listOf("content://media/1"),
            permanentlyDeletedUris = listOf("content://media/2"),
        )

        assertFalse(result.changed)
        assertEquals(setOf("content://media/1"), result.favorites)
    }

    @Test
    fun emptyPurgeNeverChangesFavorites() {
        val result = GalleryFavoritePurgePolicy.removePurged(
            currentFavorites = listOf("content://media/1"),
            permanentlyDeletedUris = emptyList(),
        )

        assertFalse(result.changed)
        assertEquals(setOf("content://media/1"), result.favorites)
    }
}
