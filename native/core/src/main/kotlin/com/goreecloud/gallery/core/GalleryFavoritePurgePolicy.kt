package com.goreecloud.gallery.core

/**
 * Computes the local Favorites state that remains valid after Android confirms permanent media
 * deletion. Android MediaStore owns deletion authority; this policy only removes now-invalid local
 * URI references and never grants media access or mutation authority.
 */
object GalleryFavoritePurgePolicy {
    data class Result(
        val favorites: Set<String>,
        val changed: Boolean,
    )

    fun removePurged(
        currentFavorites: Collection<String>,
        permanentlyDeletedUris: Collection<String>,
    ): Result {
        val current = currentFavorites.toCollection(linkedSetOf())
        if (permanentlyDeletedUris.isEmpty()) {
            return Result(favorites = current, changed = false)
        }

        val remaining = current.toMutableSet()
        val changed = remaining.removeAll(permanentlyDeletedUris.toSet())
        return Result(
            favorites = remaining.toCollection(linkedSetOf()),
            changed = changed,
        )
    }
}
