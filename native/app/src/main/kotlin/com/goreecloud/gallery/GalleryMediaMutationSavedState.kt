package com.goreecloud.gallery

import com.goreecloud.gallery.android.AndroidMediaMutationPendingState
import com.goreecloud.gallery.android.AndroidMediaMutationPendingStates

/**
 * Versioned Activity-state envelope for an already-authorized Android MediaStore mutation.
 *
 * This is storage metadata only. It does not create mutation authority and it deliberately routes
 * restoration back through [GalleryMediaMutationPendingPolicy]. Activity integration is a separate
 * lifecycle step so this contract can be validated independently before it becomes persisted UI
 * state.
 */
internal class GalleryMediaMutationSavedState internal constructor(
    val schemaVersion: Int,
    val modeName: String,
    contentUris: Array<String>,
) {
    private val frozenContentUris = contentUris.copyOf()

    fun contentUriValues(): Array<String> = frozenContentUris.copyOf()
}

internal object GalleryMediaMutationSavedStates {
    const val SCHEMA_VERSION = 1

    fun capture(state: AndroidMediaMutationPendingState): GalleryMediaMutationSavedState =
        GalleryMediaMutationSavedState(
            schemaVersion = SCHEMA_VERSION,
            modeName = AndroidMediaMutationPendingStates.modeName(state),
            contentUris = AndroidMediaMutationPendingStates.contentUriValues(state),
        )

    fun restore(
        schemaVersion: Int?,
        modeName: String?,
        contentUris: Collection<String>?,
    ): AndroidMediaMutationPendingState? {
        if (schemaVersion != SCHEMA_VERSION) return null
        return GalleryMediaMutationPendingPolicy.restore(modeName, contentUris)
    }
}
