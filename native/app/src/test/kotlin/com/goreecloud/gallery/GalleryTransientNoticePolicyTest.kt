package com.goreecloud.gallery

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GalleryTransientNoticePolicyTest {
    @Test
    fun `normalizes whitespace and blank messages`() {
        assertEquals("", GalleryTransientNoticePolicy.normalize("  \n  \t "))
        assertEquals(
            "Gallery settings imported",
            GalleryTransientNoticePolicy.normalize("  Gallery   settings\nimported  "),
        )
    }

    @Test
    fun `bounds messages without splitting unicode code points`() {
        val source = "😀".repeat(GalleryTransientNoticePolicy.MaxMessageCodePoints + 20)
        val normalized = GalleryTransientNoticePolicy.normalize(source)

        assertTrue(normalized.endsWith("…"))
        assertEquals(
            GalleryTransientNoticePolicy.MaxMessageCodePoints,
            normalized.codePointCount(0, normalized.length),
        )
    }
}
