package com.goreecloud.gallery

import com.goreecloud.gallery.android.AndroidMediaMutationMode
import com.goreecloud.gallery.android.AndroidMediaMutationPendingStates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GalleryMediaMutationSavedStateTest {
    @Test
    fun `capture and restore round trip exact pending state`() {
        val uri = "content://media/external/images/media/42"
        val pending = AndroidMediaMutationPendingStates.capture(
            AndroidMediaMutationMode.TRASH,
            listOf(uri),
        )

        val saved = GalleryMediaMutationSavedStates.capture(pending)
        val restored = GalleryMediaMutationSavedStates.restore(
            saved.schemaVersion,
            saved.modeName,
            saved.contentUriValues().asList(),
        )

        assertEquals(GalleryMediaMutationSavedStates.SCHEMA_VERSION, saved.schemaVersion)
        assertEquals(AndroidMediaMutationMode.TRASH, restored?.mode)
        assertEquals(listOf(uri), restored?.contentUris)
    }

    @Test
    fun `saved content uri values are defensively copied`() {
        val uri = "content://media/external/images/media/42"
        val pending = AndroidMediaMutationPendingStates.capture(
            AndroidMediaMutationMode.DELETE,
            listOf(uri),
        )
        val saved = GalleryMediaMutationSavedStates.capture(pending)

        val first = saved.contentUriValues()
        first[0] = "content://media/external/images/media/99"

        assertEquals(arrayOf(uri).toList(), saved.contentUriValues().toList())
    }

    @Test
    fun `restore rejects missing or unsupported schema version`() {
        val uri = "content://media/external/images/media/42"

        assertNull(GalleryMediaMutationSavedStates.restore(null, "TRASH", listOf(uri)))
        assertNull(GalleryMediaMutationSavedStates.restore(0, "TRASH", listOf(uri)))
        assertNull(
            GalleryMediaMutationSavedStates.restore(
                GalleryMediaMutationSavedStates.SCHEMA_VERSION + 1,
                "TRASH",
                listOf(uri),
            ),
        )
    }

    @Test
    fun `restore cannot manufacture recycle bin restore authority`() {
        val uri = "content://media/external/images/media/42"

        assertNull(
            GalleryMediaMutationSavedStates.restore(
                GalleryMediaMutationSavedStates.SCHEMA_VERSION,
                "RESTORE",
                listOf(uri),
            ),
        )
    }

    @Test
    fun `restore fails closed on tampered uri scope`() {
        val uri = "content://media/external/images/media/42"

        assertNull(
            GalleryMediaMutationSavedStates.restore(
                GalleryMediaMutationSavedStates.SCHEMA_VERSION,
                "DELETE",
                listOf(" $uri"),
            ),
        )
    }
}
