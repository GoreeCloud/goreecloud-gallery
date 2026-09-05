package com.goreecloud.gallery.android

/**
 * Minimal state that may be retained while Android owns a MediaStore mutation confirmation flow.
 *
 * This is not a second Trash database and does not grant mutation authority. It only preserves the
 * exact already-requested mutation mode and bounded canonical MediaStore item URIs so an Activity
 * can reconcile Android's later result after ordinary recreation. The actual mutation remains
 * owned and confirmed by Android MediaStore.
 */
data class AndroidMediaMutationPendingState internal constructor(
    val mode: AndroidMediaMutationMode,
    val contentUris: List<String>,
)

object AndroidMediaMutationPendingStates {
    fun capture(
        mode: AndroidMediaMutationMode,
        contentUris: Collection<String>,
    ): AndroidMediaMutationPendingState = AndroidMediaMutationPendingState(
        mode = mode,
        contentUris = AndroidMediaMutationRequests.normalizeMediaStoreUris(contentUris),
    )

    /**
     * Restore only exact enum state and the same fail-closed MediaStore URI shape accepted by a
     * live mutation request. Missing, unknown, malformed, broad, or oversized state is discarded.
     */
    fun restore(
        modeName: String?,
        contentUris: Collection<String>?,
    ): AndroidMediaMutationPendingState? {
        if (modeName.isNullOrBlank() || contentUris.isNullOrEmpty()) return null
        val mode = AndroidMediaMutationMode.entries.firstOrNull { it.name == modeName } ?: return null
        return try {
            capture(mode, contentUris)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun modeName(state: AndroidMediaMutationPendingState): String = state.mode.name

    fun contentUriValues(state: AndroidMediaMutationPendingState): Array<String> =
        state.contentUris.toTypedArray()
}
