package com.goreecloud.gallery.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MediaGridTest {
    @Test
    fun twoColumnGridPreservesAuthorizedPresentationOrder() {
        assertEquals(
            listOf(listOf("a", "b"), listOf("c", "d"), listOf("e")),
            mediaGridRows(listOf("a", "b", "c", "d", "e")),
        )
    }

    @Test
    fun customColumnCountRemainsStableAndBounded() {
        assertEquals(
            listOf(listOf(1, 2, 3), listOf(4, 5)),
            mediaGridRows(listOf(1, 2, 3, 4, 5), columns = 3),
        )
    }

    @Test
    fun gridRejectsNonPositiveColumnCounts() {
        assertFailsWith<IllegalArgumentException> { mediaGridRows(listOf("a"), columns = 0) }
    }
}
