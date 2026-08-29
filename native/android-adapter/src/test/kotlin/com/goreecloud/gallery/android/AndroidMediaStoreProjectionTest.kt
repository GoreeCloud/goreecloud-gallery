package com.goreecloud.gallery.android

import com.goreecloud.gallery.core.MediaStoreProjection
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidMediaStoreProjectionTest {
    @Test
    fun `android framework columns match native core projection`() {
        assertEquals(MediaStoreProjection.columns, AndroidMediaStoreProjection.columns)
        assertEquals("datetaken", AndroidMediaStoreProjection.columns[3])
    }
}
