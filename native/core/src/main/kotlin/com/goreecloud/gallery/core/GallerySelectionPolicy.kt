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

    /**
     * Resolves an explicit viewer/action request only when every requested URI still belongs to the
     * exact caller-supplied current presentation scope.
     *
     * Unlike bulk selection, explicit one-item actions must not silently shrink a stale request to a
     * different set. Duplicate URIs in either side are also ambiguous and fail closed. Returned
     * values come from the current scope rather than the older caller snapshot.
     */
    fun resolveExactCurrentScope(
        currentScope: List<MediaItem>,
        requestedItems: List<MediaItem>,
    ): List<MediaItem>? {
        if (requestedItems.isEmpty()) return emptyList()

        val currentByUri = LinkedHashMap<String, MediaItem>(currentScope.size)
        currentScope.forEach { item ->
            if (currentByUri.put(item.contentUri, item) != null) return null
        }

        val requestedUris = linkedSetOf<String>()
        val resolved = ArrayList<MediaItem>(requestedItems.size)
        requestedItems.forEach { requested ->
            if (!requestedUris.add(requested.contentUri)) return null
            val current = currentByUri[requested.contentUri] ?: return null
            resolved += current
        }
        return resolved
    }
}
