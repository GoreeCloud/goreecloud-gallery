package com.goreecloud.gallery

enum class GalleryMediaAccessScope {
    DENIED,
    LEGACY_FULL,
    SELECTED,
    IMAGES,
    VIDEOS,
    IMAGES_AND_VIDEOS,
}

data class GalleryMediaPermissionSnapshot(
    val apiLevel: Int,
    val readExternalStorage: Boolean = false,
    val readMediaImages: Boolean = false,
    val readMediaVideo: Boolean = false,
    val readMediaVisualUserSelected: Boolean = false,
)

object GalleryMediaAccessPolicy {
    fun resolve(snapshot: GalleryMediaPermissionSnapshot): GalleryMediaAccessScope = when {
        snapshot.apiLevel >= 33 && snapshot.readMediaImages && snapshot.readMediaVideo ->
            GalleryMediaAccessScope.IMAGES_AND_VIDEOS
        snapshot.apiLevel >= 33 && snapshot.readMediaImages -> GalleryMediaAccessScope.IMAGES
        snapshot.apiLevel >= 33 && snapshot.readMediaVideo -> GalleryMediaAccessScope.VIDEOS
        snapshot.apiLevel >= 34 && snapshot.readMediaVisualUserSelected -> GalleryMediaAccessScope.SELECTED
        snapshot.apiLevel <= 32 && snapshot.readExternalStorage -> GalleryMediaAccessScope.LEGACY_FULL
        else -> GalleryMediaAccessScope.DENIED
    }

    fun canRead(scope: GalleryMediaAccessScope): Boolean = scope != GalleryMediaAccessScope.DENIED

    fun isPartial(scope: GalleryMediaAccessScope): Boolean = scope == GalleryMediaAccessScope.SELECTED
}
