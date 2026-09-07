package com.goreecloud.gallery

import com.goreecloud.gallery.android.AndroidMediaMutationMode
import com.goreecloud.gallery.android.AndroidMediaMutationPendingState
import com.goreecloud.gallery.android.AndroidMediaMutationPendingStates

/**
 * GalleryActivity-specific boundary for restoring an Android-owned MediaStore mutation request.
 *
 * The main Gallery surface may originate only ordinary Trash or permanent Delete requests. Saved
 * Activity state therefore must never manufacture Recycle Bin Restore authority after recreation.
 */
internal object GalleryMediaMutationPendingPolicy {
    fun restore(
        modeName: String?,
        contentUris: Collection<String>?,
    ): AndroidMediaMutationPendingState? =
        AndroidMediaMutationPendingStates.restore(modeName, contentUris)?.takeIf { state ->
            state.mode == AndroidMediaMutationMode.TRASH || state.mode == AndroidMediaMutationMode.DELETE
        }
}
