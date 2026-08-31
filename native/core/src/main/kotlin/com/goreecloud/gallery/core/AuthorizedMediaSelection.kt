package com.goreecloud.gallery.core

/**
 * Device-local selection state bounded to the Gallery-authorized media snapshot.
 *
 * This class owns no MediaStore, mutation, sharing, deletion, or persistence authority.
 * It only tracks IDs that remain present in the caller-supplied authorized snapshot.
 */
class AuthorizedMediaSelection(initialAuthorizedItems: List<MediaItem> = emptyList()) {
    private var authorizedById: Map<String, MediaItem> = indexAuthorized(initialAuthorizedItems)
    private val selectedIds = linkedSetOf<String>()

    val size: Int
        get() = selectedIds.size

    val isEmpty: Boolean
        get() = selectedIds.isEmpty()

    fun isSelected(itemId: String): Boolean = itemId in selectedIds

    /** Returns false and makes no change when the ID is outside the authorized snapshot. */
    fun select(itemId: String): Boolean {
        if (itemId !in authorizedById) return false
        return selectedIds.add(itemId)
    }

    /** Returns false and makes no change when the ID is outside the authorized snapshot. */
    fun deselect(itemId: String): Boolean {
        if (itemId !in authorizedById) return false
        return selectedIds.remove(itemId)
    }

    /**
     * Toggles only authorized IDs. The returned value is the resulting selected state;
     * null means the requested ID is outside the current authorized snapshot.
     */
    fun toggle(itemId: String): Boolean? {
        if (itemId !in authorizedById) return null
        return if (selectedIds.remove(itemId)) false else {
            selectedIds.add(itemId)
            true
        }
    }

    fun clear() {
        selectedIds.clear()
    }

    /**
     * Replaces the authorization snapshot and immediately prunes stale selection.
     * This is intended for permission changes and refreshed MediaStore snapshots.
     */
    fun replaceAuthorizedSnapshot(items: List<MediaItem>) {
        authorizedById = indexAuthorized(items)
        selectedIds.retainAll(authorizedById.keys)
    }

    /** Returns selected items in current authorized snapshot order. */
    fun selectedItems(): List<MediaItem> = authorizedById.values.filter { it.id in selectedIds }

    /**
     * Returns the selected subset of a rendered collection, bounded again to current
     * authorization. This lets later contextual actions operate only on items that are
     * both selected and deliberately presented to the user.
     */
    fun selectedPresentedItems(presentedItems: List<MediaItem>): List<MediaItem> = presentedItems.filter { item ->
        item.id in selectedIds && authorizedById[item.id]?.contentUri == item.contentUri
    }

    private fun indexAuthorized(items: List<MediaItem>): Map<String, MediaItem> = buildMap {
        items.forEach { item ->
            require(item.id !in this) { "authorized media snapshot contains duplicate item id ${item.id}" }
            put(item.id, item)
        }
    }
}
