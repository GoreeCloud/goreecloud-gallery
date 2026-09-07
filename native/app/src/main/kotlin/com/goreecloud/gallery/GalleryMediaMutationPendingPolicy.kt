package com.goreecloud.gallery

import com.goreecloud.gallery.android.AndroidMediaMutationMode
import com.goreecloud.gallery.android.AndroidMediaMutationPendingState
import com.goreecloud.gallery.android.AndroidMediaMutationPendingStates

/**
 * GalleryActivity-specific boundary for restoring an Android-owned MediaStore mutation request.
 *
 * The main Gallery surface may originate only ordinary Trash or permanent Delete requests. Saved
 * Activity state therefore must never manufacture Recycle Bin Restore authority after recreation.
 * Restoration also stays deliberately bounded so malformed or unexpectedly large saved state is
 * rejected instead of being treated as media authority after Activity recreation.
 */
internal object GalleryMediaMutationPendingPolicy {
    internal const val MAX_RESTORED_CONTENT_URIS = 512

    fun restore(
        modeName: String?,
        contentUris: Collection<String>?,
    ): AndroidMediaMutationPendingState? {
        if (contentUris == null || contentUris.size !in 1..MAX_RESTORED_CONTENT_URIS) return null

        return AndroidMediaMutationPendingStates.restore(modeName, contentUris)?.takeIf { state ->
            state.mode == AndroidMediaMutationMode.TRASH || state.mode == AndroidMediaMutationMode.DELETE
        }
    }
}
