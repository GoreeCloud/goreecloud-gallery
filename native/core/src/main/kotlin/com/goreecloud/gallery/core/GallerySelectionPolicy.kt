package com.goreecloud.gallery.core

/**
 * Pure selection policy for the native Gallery browsing surfaces.
 *
 * Selection is presentation state over a caller-supplied current scope. It never creates media
 * authority: content URIs that are not in the current scope are pruned and cannot be resolved for
 * bulk actions.
 */
object GallerySelectionPolicy {
    fun toggle(
        selectedContentUris: Set<String>,
        item: MediaItem,
        currentScope: List<MediaItem>,
    ): Set<String> {
        val valid = prune(selectedContentUris, currentScope).toMutableSet()
        if (currentScope.none { it.contentUri == item.contentUri }) return valid

        if (!valid.add(item.contentUri)) valid.remove(item.contentUri)
        return valid
    }

    fun selectAll(currentScope: List<MediaItem>): Set<String> =
        currentScope.mapTo(linkedSetOf()) { it.contentUri }

    fun prune(selectedContentUris: Set<String>, currentScope: List<MediaItem>): Set<String> {
        if (selectedContentUris.isEmpty() || currentScope.isEmpty()) return emptySet()
        val allowed = currentScope.asSequence().map { it.contentUri }.toHashSet()
        return selectedContentUris.filterTo(linkedSetOf()) { it in allowed }
    }

    fun resolve(currentScope: List<MediaItem>, selectedContentUris: Set<String>): List<MediaItem> {
        if (selectedContentUris.isEmpty()) return emptyList()
        return currentScope.filter { it.contentUri in selectedContentUris }
    }
}
