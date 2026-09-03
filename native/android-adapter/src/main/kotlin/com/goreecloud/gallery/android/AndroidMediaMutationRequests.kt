package com.goreecloud.gallery.android

import android.app.PendingIntent
import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.net.URI

/**
 * Creates Android-owned confirmation requests for destructive MediaStore operations.
 *
 * GoreeCloud Gallery never turns a rendered/selected item into direct filesystem authority.
 * Only bounded MediaStore content URIs from the current authorized presentation scope may
 * reach this boundary, and Android remains responsible for the destructive confirmation UI.
 */
enum class AndroidMediaMutationMode {
    TRASH,
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
            "Android-authorized trash/delete requests require Android 11 or newer"
        }

        val normalizedUris = normalizeMediaStoreUris(contentUris)
        val androidUris = normalizedUris.map(Uri::parse)
        val pendingIntent = when (mode) {
            AndroidMediaMutationMode.TRASH ->
                MediaStore.createTrashRequest(contentResolver, androidUris, true)
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
            normalized += value
        }

        require(normalized.size <= MAX_MUTATION_ITEMS) {
            "a single media mutation is limited to $MAX_MUTATION_ITEMS items"
        }
        return normalized.toList()
    }
}
