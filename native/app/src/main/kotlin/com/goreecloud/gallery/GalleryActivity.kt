package com.goreecloud.gallery

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.LruCache
import android.util.Size
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.goreecloud.gallery.android.AndroidMediaStoreReader
import com.goreecloud.gallery.core.MediaItem
import com.goreecloud.gallery.core.MediaSortOrder
import com.goreecloud.gallery.core.MediaTypeFilter
import com.goreecloud.gallery.core.filter
import com.goreecloud.gallery.core.sort
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class GalleryActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var action: Button
    private lateinit var filterRow: LinearLayout
    private lateinit var sortRow: LinearLayout
    private lateinit var library: LinearLayout
    private val thumbnailExecutor = Executors.newFixedThreadPool(THUMBNAIL_WORKERS)
    private val thumbnailCache = object : LruCache<String, Bitmap>(THUMBNAIL_CACHE_KIB) {
        override fun sizeOf(key: String, value: Bitmap): Int = maxOf(1, value.allocationByteCount / 1024)
    }
    private var loadGeneration = 0
    private var authorizedItems: List<MediaItem> = emptyList()
    private var selectedFilter = MediaTypeFilter.ALL
    private var selectedSort = MediaSortOrder.NEWEST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildSurface()
    }

    override fun onResume() {
        super.onResume()
        renderPermissionState()
    }

    override fun onDestroy() {
        thumbnailExecutor.shutdownNow()
        thumbnailCache.evictAll()
        super.onDestroy()
    }

    private fun buildSurface() {
        val canvas = themeColor(android.R.attr.colorBackground, 0xfffafafa.toInt())
        val primaryTextColor = themeColor(android.R.attr.textColorPrimary, 0xff1d1d1f.toInt())
        val secondaryTextColor = themeColor(android.R.attr.textColorSecondary, 0xff666666.toInt())
        val gutter = dp(GalleryGlazeContract.horizontalGutterDp(resources.configuration.screenWidthDp))

        window.statusBarColor = canvas
        window.navigationBarColor = canvas
        val night = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        @Suppress("DEPRECATION")
        run {
            var flags = 0
            if (!night) {
                flags = flags or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) flags = flags or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            window.decorView.systemUiVisibility = flags
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(gutter, dp(24), gutter, dp(40))
            setBackgroundColor(canvas)
        }

        content.addView(TextView(this).apply {
            text = "Local library"
            setTextColor(secondaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            isAllCaps = false
            importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
        })
        content.addView(TextView(this).apply {
            text = "Gallery"
            setTextColor(primaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 34f)
            setPadding(0, dp(4), 0, 0)
        })
        content.addView(TextView(this).apply {
            text = "First-party Android shell · Glaze UI ${GalleryGlazeContract.VERSION} source target"
            setTextColor(secondaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setPadding(0, dp(8), 0, dp(18))
        })

        status = TextView(this).apply {
            setTextColor(primaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = roundedSurface(themeColor(android.R.attr.colorControlHighlight, 0x14000000), 16)
            importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        content.addView(status, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        action = Button(this).apply {
            minHeight = dp(GalleryGlazeContract.GENERAL_TARGET_DP)
            isAllCaps = false
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            contentDescription = "Gallery media access action"
        }
        content.addView(action, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) })

        content.addView(TextView(this).apply {
            text = "Recent media"
            setTextColor(primaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setPadding(0, dp(26), 0, dp(4))
        })
        content.addView(TextView(this).apply {
            text = "Newest authorized MediaStore rows and local thumbnails are shown without network access or cloud dependency. Type filters and sort controls operate only on this authorized snapshot; preview navigation stays within the presented view."
            setTextColor(secondaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(0, 0, 0, dp(8))
        })

        filterRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
        }
        content.addView(filterRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        renderFilterControls()

        sortRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
        }
        content.addView(sortRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(6)
        })
        renderSortControls()

        library = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(library, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        })
    }

    private fun renderFilterControls() {
        if (!::filterRow.isInitialized) return
        filterRow.removeAllViews()
        MediaTypeFilter.entries.forEachIndexed { index, filter ->
            val selected = filter == selectedFilter
            val button = Button(this).apply {
                text = when (filter) {
                    MediaTypeFilter.ALL -> "All"
                    MediaTypeFilter.IMAGES -> "Images"
                    MediaTypeFilter.VIDEOS -> "Videos"
                }
                isAllCaps = false
                minHeight = dp(GalleryGlazeContract.GENERAL_TARGET_DP)
                isEnabled = authorizedItems.isNotEmpty()
                isActivated = selected
                alpha = if (selected) 1f else 0.72f
                contentDescription = "Show ${text.toString().lowercase()} in the current authorized library"
                setOnClickListener {
                    selectedFilter = filter
                    renderFilterControls()
                    renderAuthorizedSnapshot(loadGeneration)
                }
            }
            filterRow.addView(button, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                if (index > 0) marginStart = dp(6)
            })
        }
    }

    private fun renderSortControls() {
        if (!::sortRow.isInitialized) return
        sortRow.removeAllViews()
        MediaSortOrder.entries.forEachIndexed { index, sortOrder ->
            val selected = sortOrder == selectedSort
            val button = Button(this).apply {
                text = when (sortOrder) {
                    MediaSortOrder.NEWEST -> "Newest first"
                    MediaSortOrder.OLDEST -> "Oldest first"
                }
                isAllCaps = false
                minHeight = dp(GalleryGlazeContract.GENERAL_TARGET_DP)
                isEnabled = authorizedItems.isNotEmpty()
                isActivated = selected
                alpha = if (selected) 1f else 0.72f
                contentDescription = "Sort the current authorized library ${text.toString().lowercase()}"
                setOnClickListener {
                    selectedSort = sortOrder
                    renderSortControls()
                    renderAuthorizedSnapshot(loadGeneration)
                }
            }
            sortRow.addView(button, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                if (index > 0) marginStart = dp(6)
            })
        }
    }

    private fun renderPermissionState() {
        val accessScope = currentMediaAccessScope()
        if (!GalleryMediaAccessPolicy.canRead(accessScope)) {
            loadGeneration += 1
            authorizedItems = emptyList()
            status.text = "Media permission is required before Gallery can read the local library."
            action.isEnabled = true
            action.text = "Choose media access"
            action.setOnClickListener { requestReadableMediaAccess() }
            renderFilterControls()
            renderSortControls()
            library.removeAllViews()
            library.addView(messageRow("No MediaStore query has been attempted."))
            return
        }

        if (GalleryMediaAccessPolicy.isPartial(accessScope)) {
            action.text = "Change selected media"
            action.setOnClickListener { requestReadableMediaAccess() }
        } else {
            action.text = "Refresh local library"
            action.setOnClickListener { loadLocalLibrary(accessScope) }
        }
        loadLocalLibrary(accessScope)
    }

    private fun loadLocalLibrary(accessScope: GalleryMediaAccessScope) {
        val generation = ++loadGeneration
        action.isEnabled = false
        status.text = "${accessScopeLabel(accessScope)} · Reading the authorized local MediaStore view…"
        library.removeAllViews()
        library.addView(messageRow("Loading local media…"))

        thread(name = "goreecloud-gallery-mediastore") {
            try {
                val result = AndroidMediaStoreReader(contentResolver).readLatest(GalleryGlazeContract.MAX_RENDERED_MEDIA_ROWS)
                runOnUiThread {
                    if (generation != loadGeneration) return@runOnUiThread
                    authorizedItems = result.items
                    action.isEnabled = true
                    status.text = buildString {
                        append(accessScopeLabel(accessScope))
                        append(" · Authorized local library: ${result.items.size} item")
                        if (result.items.size != 1) append('s')
                        if (result.rejectedRowCount > 0) append(" · ${result.rejectedRowCount} malformed row(s) skipped")
                    }
                    renderFilterControls()
                    renderSortControls()
                    renderAuthorizedSnapshot(generation)
                }
            } catch (_: SecurityException) {
                renderLoadFailure(generation, "Android denied the current MediaStore read. Review media access and try again.")
            } catch (_: RuntimeException) {
                renderLoadFailure(generation, "The local MediaStore query is unavailable. Gallery is not treating that failure as an empty library.")
            }
        }
    }

    private fun renderLoadFailure(generation: Int, message: String) {
        runOnUiThread {
            if (generation != loadGeneration) return@runOnUiThread
            authorizedItems = emptyList()
            action.isEnabled = true
            status.text = message
            renderFilterControls()
            renderSortControls()
            library.removeAllViews()
            library.addView(messageRow("No media list is shown because the authoritative provider read did not succeed."))
        }
    }

    private fun renderAuthorizedSnapshot(generation: Int) {
        if (generation != loadGeneration) return
        val presentedItems = selectedSort.sort(selectedFilter.filter(authorizedItems))
        library.removeAllViews()
        if (presentedItems.isEmpty()) {
            val label = when (selectedFilter) {
                MediaTypeFilter.ALL -> "image or video"
                MediaTypeFilter.IMAGES -> "image"
                MediaTypeFilter.VIDEOS -> "video"
            }
            library.addView(messageRow("No authorized $label rows are present in the current snapshot."))
            return
        }
        presentedItems.forEachIndexed { index, item -> library.addView(mediaRow(item, presentedItems, index, generation)) }
    }

    private fun mediaRow(item: MediaItem, items: List<MediaItem>, index: Int, generation: Int): LinearLayout {
        val primaryTextColor = themeColor(android.R.attr.textColorPrimary, 0xff1d1d1f.toInt())
        val secondaryTextColor = themeColor(android.R.attr.textColorSecondary, 0xff666666.toInt())
        val surface = themeColor(android.R.attr.colorBackgroundFloating, themeColor(android.R.attr.colorBackground, 0xfffafafa.toInt()))
        val rowCacheKey = thumbnailCacheKey(ROW_THUMBNAIL_NAMESPACE, item.contentUri)
        val thumbnail = ImageView(this).apply {
            tag = rowCacheKey
            contentDescription = "Thumbnail for ${item.displayName}"
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = roundedSurface(themeColor(android.R.attr.colorControlHighlight, 0x14000000), 14)
        }
        loadLocalThumbnail(item, thumbnail, generation, THUMBNAIL_DP, ROW_THUMBNAIL_NAMESPACE)

        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(context).apply {
                text = item.displayName
                setTextColor(primaryTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            })
            addView(TextView(context).apply {
                text = mediaMetadata(item)
                setTextColor(secondaryTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setPadding(0, dp(5), 0, 0)
            })
        }

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(14), dp(10))
            background = roundedSurface(surface, 18)
            importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
            isClickable = true
            isFocusable = true
            contentDescription = "Open local preview for ${item.displayName}"
            setOnClickListener { showAuthorizedPreview(items, index, generation) }
            addView(thumbnail, LinearLayout.LayoutParams(dp(THUMBNAIL_DP), dp(THUMBNAIL_DP)).apply { marginEnd = dp(12) })
            addView(details, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) }
        }
    }

    private fun showAuthorizedPreview(items: List<MediaItem>, initialIndex: Int, generation: Int) {
        if (generation != loadGeneration || !GalleryMediaAccessPolicy.canRead(currentMediaAccessScope()) || initialIndex !in items.indices) return

        val primaryTextColor = themeColor(android.R.attr.textColorPrimary, 0xff1d1d1f.toInt())
        val secondaryTextColor = themeColor(android.R.attr.textColorSecondary, 0xff666666.toInt())
        val preview = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
            minimumHeight = dp(VIEWER_PREVIEW_DP)
            background = roundedSurface(themeColor(android.R.attr.colorControlHighlight, 0x14000000), 18)
        }
        val metadata = TextView(this).apply {
            setTextColor(secondaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(0, dp(12), 0, 0)
        }
        val note = TextView(this).apply {
            setTextColor(secondaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(0, dp(8), 0, 0)
        }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(4))
            addView(preview, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(VIEWER_PREVIEW_DP)))
            addView(metadata)
            addView(note)
        }

        var currentIndex = initialIndex
        val dialog = AlertDialog.Builder(this)
            .setTitle(items[currentIndex].displayName)
            .setView(body)
            .setNeutralButton("Previous", null)
            .setPositiveButton("Next", null)
            .setNegativeButton("Close", null)
            .create()

        dialog.setOnShowListener {
            val previousButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            val nextButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val closeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            listOf(previousButton, nextButton, closeButton).forEach { button ->
                button?.apply {
                    minHeight = dp(GalleryGlazeContract.GENERAL_TARGET_DP)
                    setTextColor(primaryTextColor)
                }
            }

            fun renderCurrentItem() {
                if (generation != loadGeneration || !GalleryMediaAccessPolicy.canRead(currentMediaAccessScope()) || currentIndex !in items.indices) {
                    dialog.dismiss()
                    return
                }
                val item = items[currentIndex]
                val viewerCacheKey = thumbnailCacheKey(VIEWER_THUMBNAIL_NAMESPACE, item.contentUri)
                dialog.setTitle(item.displayName)
                preview.setImageDrawable(null)
                preview.tag = viewerCacheKey
                preview.contentDescription = "Local preview for ${item.displayName}"
                metadata.text = mediaMetadata(item)
                note.text = if (item.mimeType.startsWith("video/")) {
                    "Local video poster preview only. Playback is a separate milestone."
                } else {
                    "Bounded local preview only. Full-resolution viewing and editing are separate milestones."
                }
                previousButton?.isEnabled = currentIndex > 0
                nextButton?.isEnabled = currentIndex < items.lastIndex
                loadLocalThumbnail(item, preview, generation, VIEWER_PREVIEW_DP, VIEWER_THUMBNAIL_NAMESPACE)
            }
            previousButton?.setOnClickListener { if (currentIndex > 0) { currentIndex -= 1; renderCurrentItem() } }
            nextButton?.setOnClickListener { if (currentIndex < items.lastIndex) { currentIndex += 1; renderCurrentItem() } }
            renderCurrentItem()
        }
        dialog.show()
    }

    private fun mediaMetadata(item: MediaItem): String {
        val timestamp = item.capturedAt ?: item.modifiedAt
        val kind = if (item.mimeType.startsWith("video/")) "Video" else "Image"
        return listOfNotNull(kind, item.albumName?.let { "Album: $it" }, DATE_TIME_FORMAT.format(timestamp), formatBytes(item.sizeBytes)).joinToString(" · ")
    }

    private fun loadLocalThumbnail(item: MediaItem, target: ImageView, generation: Int, sizeDp: Int, namespace: String) {
        val cacheKey = thumbnailCacheKey(namespace, item.contentUri)
        thumbnailCache.get(cacheKey)?.let { cached ->
            if (generation == loadGeneration && target.tag == cacheKey) target.setImageBitmap(cached)
            return
        }
        thumbnailExecutor.execute {
            val bitmap = try {
                contentResolver.loadThumbnail(Uri.parse(item.contentUri), Size(dp(sizeDp), dp(sizeDp)), null)
            } catch (_: SecurityException) {
                null
            } catch (_: RuntimeException) {
                null
            }
            if (bitmap == null) return@execute
            thumbnailCache.put(cacheKey, bitmap)
            runOnUiThread { if (generation == loadGeneration && target.tag == cacheKey) target.setImageBitmap(bitmap) }
        }
    }

    private fun thumbnailCacheKey(namespace: String, contentUri: String): String = "$namespace:$contentUri"

    private fun messageRow(message: String): TextView = TextView(this).apply {
        text = message
        setTextColor(themeColor(android.R.attr.textColorSecondary, 0xff666666.toInt()))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        gravity = Gravity.START
        setPadding(0, dp(10), 0, dp(10))
    }

    private fun currentMediaAccessScope(): GalleryMediaAccessScope {
        fun granted(permission: String) = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        return GalleryMediaAccessPolicy.resolve(
            GalleryMediaPermissionSnapshot(
                apiLevel = Build.VERSION.SDK_INT,
                readExternalStorage = Build.VERSION.SDK_INT <= 32 && granted(Manifest.permission.READ_EXTERNAL_STORAGE),
                readMediaImages = Build.VERSION.SDK_INT >= 33 && granted(Manifest.permission.READ_MEDIA_IMAGES),
                readMediaVideo = Build.VERSION.SDK_INT >= 33 && granted(Manifest.permission.READ_MEDIA_VIDEO),
                readMediaVisualUserSelected = Build.VERSION.SDK_INT >= 34 && granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED),
            ),
        )
    }

    private fun accessScopeLabel(scope: GalleryMediaAccessScope): String = when (scope) {
        GalleryMediaAccessScope.DENIED -> "Media access denied"
        GalleryMediaAccessScope.LEGACY_FULL -> "Authorized local media access"
        GalleryMediaAccessScope.SELECTED -> "Selected media only"
        GalleryMediaAccessScope.IMAGES -> "Images authorized"
        GalleryMediaAccessScope.VIDEOS -> "Videos authorized"
        GalleryMediaAccessScope.IMAGES_AND_VIDEOS -> "Images and videos authorized"
    }

    private fun requestReadableMediaAccess() {
        val permissions = when {
            Build.VERSION.SDK_INT >= 34 -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            )
            Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissions(permissions, MEDIA_PERMISSION_REQUEST)
    }

    private fun roundedSurface(color: Int, radiusDp: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun themeColor(attribute: Int, fallback: Int): Int {
        val value = TypedValue()
        return if (theme.resolveAttribute(attribute, value, true)) value.data else fallback
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private companion object {
        const val MEDIA_PERMISSION_REQUEST = 4101
        const val THUMBNAIL_DP = 76
        const val VIEWER_PREVIEW_DP = 320
        const val THUMBNAIL_WORKERS = 2
        const val THUMBNAIL_CACHE_KIB = 8 * 1024
        const val ROW_THUMBNAIL_NAMESPACE = "row"
        const val VIEWER_THUMBNAIL_NAMESPACE = "viewer"
        val DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a").withZone(ZoneId.systemDefault())

        fun formatBytes(bytes: Long): String = when {
            bytes >= 1024L * 1024L -> String.format("%.1f MiB", bytes / (1024.0 * 1024.0))
            bytes >= 1024L -> String.format("%.1f KiB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
