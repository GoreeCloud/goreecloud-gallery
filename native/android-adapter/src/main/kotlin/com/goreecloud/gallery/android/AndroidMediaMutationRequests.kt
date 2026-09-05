package com.goreecloud.gallery.android

import android.app.PendingIntent
import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.net.URI

/**
 * Creates Android-owned confirmation requests for MediaStore trash/recovery/destructive operations.
 *
 * GoreeCloud Gallery never turns a rendered/selected item into direct filesystem authority.
 * Only bounded MediaStore image/video item URIs from the current authorized presentation scope may
 * reach this boundary, and Android remains responsible for the final confirmation UI.
 */
enum class AndroidMediaMutationMode {
    TRASH,
    RESTORE,
    DELETE,
}

data class AndroidMediaMutationRequest(
    val mode: AndroidMediaMutationMode,
    val contentUris: List<String>,
    val pendingIntent: PendingIntent,
)

object AndroidMediaMutationRequests {
    const val MIN_SUPPORTED_API = Build.VERSION_CODES.R
    const val MAX_MUTATION_ITEMS = 100

    fun isSupported(apiLevel: Int = Build.VERSION.SDK_INT): Boolean =
        apiLevel >= MIN_SUPPORTED_API

    fun create(
        contentResolver: ContentResolver,
        contentUris: Collection<String>,
        mode: AndroidMediaMutationMode,
    ): AndroidMediaMutationRequest {
        check(isSupported()) {
            "Android-authorized trash/restore/delete requests require Android 11 or newer"
        }

        val normalizedUris = normalizeMediaStoreUris(contentUris)
        val androidUris = normalizedUris.map(Uri::parse)
        val pendingIntent = when (mode) {
            AndroidMediaMutationMode.TRASH ->
                MediaStore.createTrashRequest(contentResolver, androidUris, true)
            AndroidMediaMutationMode.RESTORE ->
                MediaStore.createTrashRequest(contentResolver, androidUris, false)
            AndroidMediaMutationMode.DELETE ->
                MediaStore.createDeleteRequest(contentResolver, androidUris)
        }
        return AndroidMediaMutationRequest(
            mode = mode,
            contentUris = normalizedUris,
            pendingIntent = pendingIntent,
        )
    }

    internal fun normalizeMediaStoreUris(contentUris: Collection<String>): List<String> {
        require(contentUris.isNotEmpty()) { "at least one media content URI is required" }

        val normalized = linkedSetOf<String>()
        contentUris.forEach { raw ->
            val value = raw.trim()
            require(value.isNotEmpty()) { "media content URIs must not be blank" }
            val parsed = try {
                URI(value)
            } catch (error: Exception) {
                throw IllegalArgumentException("invalid media content URI", error)
            }
            require(parsed.scheme == "content") { "only content URIs may be mutated" }
            require(parsed.authority == MediaStore.AUTHORITY) {
                "only Android MediaStore URIs may be mutated"
            }
            require(parsed.rawQuery == null && parsed.rawFragment == null) {
                "MediaStore mutation URIs must not include query parameters or fragments"
            }
            require(parsed.userInfo == null && parsed.port == -1) {
                "MediaStore mutation URIs must use the canonical content authority form"
            }
            requireSpecificImageOrVideoItem(parsed)
            normalized += value
        }

        require(normalized.size <= MAX_MUTATION_ITEMS) {
            "a single media mutation is limited to $MAX_MUTATION_ITEMS items"
        }
        return normalized.toList()
    }

    private fun requireSpecificImageOrVideoItem(uri: URI) {
        val segments = uri.path
            ?.split('/')
            ?.filter { it.isNotBlank() }
            .orEmpty()
        require(segments.size == 4) {
            "MediaStore mutation URI must reference one specific media item"
        }
        require(segments[0].isNotBlank()) { "MediaStore volume is required" }
        require(segments[1] == "images" || segments[1] == "video") {
            "Gallery mutations require an image or video MediaStore item URI"
        }
        require(segments[2] == "media") {
            "MediaStore mutation URI must reference the media item collection"
        }
        val itemId = segments[3].toLongOrNull()
        require(itemId != null && itemId > 0) {
            "MediaStore mutation URI must reference a positive numeric item ID"
        }
    }
}
