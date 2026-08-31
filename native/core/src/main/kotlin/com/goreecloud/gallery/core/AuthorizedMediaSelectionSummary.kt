package com.goreecloud.gallery.core

data class AuthorizedMediaSelectionSummary(
    val selectedCount: Int,
    val imageCount: Int,
    val videoCount: Int,
    val totalSizeBytes: Long,
) {
    init {
        require(selectedCount >= 0)
        require(imageCount >= 0)
        require(videoCount >= 0)
        require(imageCount + videoCount == selectedCount)
        require(totalSizeBytes >= 0)
    }
}

/**
 * Summarizes only the selected items that remain both authorized and present in the
 * caller-supplied rendered collection. This grants no mutation, sharing, deletion,
 * MediaStore, persistence, or selection authority.
 */
fun AuthorizedMediaSelection.selectionSummary(
    presentedItems: List<MediaItem>,
): AuthorizedMediaSelectionSummary {
    val selected = selectedPresentedItems(presentedItems)
    var imageCount = 0
    var videoCount = 0
    var totalSizeBytes = 0L

    selected.forEach { item ->
        when (item.kind) {
            MediaKind.IMAGE -> imageCount += 1
            MediaKind.VIDEO -> videoCount += 1
        }
        totalSizeBytes = Math.addExact(totalSizeBytes, item.sizeBytes)
    }

    return AuthorizedMediaSelectionSummary(
        selectedCount = selected.size,
        imageCount = imageCount,
        videoCount = videoCount,
        totalSizeBytes = totalSizeBytes,
    )
}
