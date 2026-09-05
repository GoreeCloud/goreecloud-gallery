package com.goreecloud.gallery

/**
 * Decides whether a Recycle Bin viewer that was opened from one authoritative MediaStore Trash
 * snapshot may remain visible after Gallery re-reads Trash on resume.
 *
 * Android 14+ can change the user-selected media set without changing Gallery's coarse permission
 * classification from SELECTED. Comparing the authoritative item URI sequence closes that gap
 * without inventing broader permission or mutation authority.
 */
object RecycleBinViewerRevalidationPolicy {
    fun requiresViewerReset(
        previousUris: List<String>,
        currentUris: List<String>,
    ): Boolean = previousUris != currentUris
}
