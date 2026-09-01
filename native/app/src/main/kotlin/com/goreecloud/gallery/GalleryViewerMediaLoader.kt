package com.goreecloud.gallery

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import com.goreecloud.gallery.core.MediaItem
import java.io.IOException

/**
 * Full-screen viewer loading boundary for an already-authorized [MediaItem].
 *
 * Image items use Android ImageDecoder first so encoded orientation is interpreted by the platform.
 * Video items retain the existing provider thumbnail path. If an authorized image cannot be decoded,
 * the bounded provider thumbnail remains a presentation fallback rather than expanding read authority.
 */
object GalleryViewerMediaLoader {
    fun load(
        contentResolver: ContentResolver,
        item: MediaItem,
        viewportWidth: Int,
        viewportHeight: Int,
        fallbackSizePx: Int,
    ): Bitmap? {
        require(viewportWidth > 0) { "viewportWidth must be positive" }
        require(viewportHeight > 0) { "viewportHeight must be positive" }
        require(fallbackSizePx > 0) { "fallbackSizePx must be positive" }

        val contentUri = Uri.parse(item.contentUri)
        if (GalleryViewerDecodePolicy.usesImageDecoder(item.mimeType)) {
            GalleryViewerImageDecoder.decodeBounded(
                contentResolver = contentResolver,
                contentUri = contentUri,
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight,
            )?.let { return it }
        }

        return try {
            contentResolver.loadThumbnail(
                contentUri,
                Size(fallbackSizePx, fallbackSizePx),
                null,
            )
        } catch (_: SecurityException) {
            null
        } catch (_: IOException) {
            null
        } catch (_: RuntimeException) {
            null
        }
    }
}
