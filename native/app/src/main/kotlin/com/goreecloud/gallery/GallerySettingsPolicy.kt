package com.goreecloud.gallery

import com.goreecloud.gallery.core.MediaItem

enum class GalleryFileLoadingPriority(
    val storedValue: String,
    val label: String,
    val thumbnailWorkerCount: Int,
) {
    SLOW("slow", "Slow", 1),
    FAST("fast", "Fast", 4),
    ;

    companion object {
        fun fromStored(value: String?): GalleryFileLoadingPriority =
            entries.firstOrNull { it.storedValue == value } ?: FAST
    }
}

data class GalleryUserSettings(
    val fileLoadingPriority: GalleryFileLoadingPriority = GalleryFileLoadingPriority.FAST,
    val includedAlbumIds: Set<String> = emptySet(),
    val excludedAlbumIds: Set<String> = emptySet(),
    val showHiddenItems: Boolean = false,
    val playVideosAutomatically: Boolean = false,
    val loopVideos: Boolean = false,
    val animateGifThumbnails: Boolean = false,
    val deleteEmptyFolders: Boolean = false,
    val moveDeletedItemsToRecycleBin: Boolean = true,
    val roundedSquareThumbnails: Boolean = true,
)

object GallerySettingsPolicy {
    const val EXPORT_SCHEMA_VERSION = 1

    fun visibleItems(items: List<MediaItem>, settings: GalleryUserSettings): List<MediaItem> =
        items.filter { item ->
            val included = settings.includedAlbumIds.isEmpty() || item.albumId in settings.includedAlbumIds
            val excluded = item.albumId != null && item.albumId in settings.excludedAlbumIds
            val hidden = isHidden(item)
            included && !excluded && (settings.showHiddenItems || !hidden)
        }

    fun isHidden(item: MediaItem): Boolean =
        item.displayName.trimStart().startsWith('.') ||
            item.albumName?.trimStart()?.startsWith('.') == true
}
