package com.goreecloud.gallery

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.LruCache
import android.util.Size
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import com.goreecloud.gallery.android.AndroidMediaStoreReader
import com.goreecloud.gallery.core.MediaItem
import com.goreecloud.gallery.core.MediaSortOrder
import com.goreecloud.gallery.core.MediaTypeFilter
import com.goreecloud.gallery.core.filter
import com.goreecloud.gallery.core.filterAuthorizedAlbum
import com.goreecloud.gallery.core.mediaAlbumOptions
import com.goreecloud.gallery.core.sort
import com.goreecloud.gallery.core.summarizeAuthorizedMediaView
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class GalleryActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var action: TextView
    private lateinit var controlsSection: LinearLayout
    private lateinit var filterRow: LinearLayout
    private lateinit var sortRow: LinearLayout
    private lateinit var albumRow: LinearLayout
    private lateinit var viewSummary: TextView
    private lateinit var resetViewButton: TextView
    private lateinit var library: LinearLayout

    private val thumbnailExecutor = Executors.newFixedThreadPool(THUMBNAIL_WORKERS)
    private val thumbnailCache = object : LruCache<String, Bitmap>(THUMBNAIL_CACHE_KIB) {
        override fun sizeOf(key: String, value: Bitmap): Int = maxOf(1, value.allocationByteCount / 1024)
    }

    private var loadGeneration = 0
    private var authorizedItems: List<MediaItem> = emptyList()
    private var selectedFilter = MediaTypeFilter.ALL
    private var selectedSort = MediaSortOrder.NEWEST
    private var selectedAlbumId: String? = null

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
        val accentColor = themeColor(android.R.attr.colorAccent, 0xff2e7d6f.toInt())
        val gutter = dp(GalleryGlazeContract.horizontalGutterDp(resources.configuration.screenWidthDp))

        window.statusBarColor = canvas
        window.navigationBarColor = canvas
        val night = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        @Suppress("DEPRECATION")
        run {
            var flags = 0
            if (!night) {
                flags = flags or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = flags or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            }
            window.decorView.systemUiVisibility = flags
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(gutter, dp(18), gutter, dp(36))
            setBackgroundColor(canvas)
        }

        content.addView(TextView(this).apply {
            text = "Gallery"
            setTextColor(primaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 34f)
            setTypeface(typeface, Typeface.BOLD)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        })
        content.addView(TextView(this).apply {
            text = "Photos and videos on this device"
            setTextColor(secondaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setPadding(0, dp(4), 0, dp(18))
        })

        val accessPanel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(10), dp(12))
            background = roundedSurface(
                withAlpha(themeColor(android.R.attr.colorControlHighlight, 0xff000000.toInt()), 0.08f),
                20,
            )
        }
        status = TextView(this).apply {
            setTextColor(primaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setLineSpacing(0f, 1.08f)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        action = TextView(this).apply {
            minHeight = dp(GalleryGlazeContract.GENERAL_TARGET_DP)
            gravity = Gravity.CENTER
            setPadding(dp(16), 0, dp(16), 0)
            setTextColor(accentColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTypeface(typeface, Typeface.BOLD)
            background = roundedSurface(withAlpha(accentColor, 0.14f), 16)
            isClickable = true
            isFocusable = true
            contentDescription = "Gallery media access action"
        }
        accessPanel.addView(
            status,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(10)
            },
        )
        accessPanel.addView(
            action,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
        )
        content.addView(
            accessPanel,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
        )

        controlsSection = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        val libraryHeading = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(24), 0, dp(8))
        }
        libraryHeading.addView(TextView(this).apply {
            text = "Library"
            setTextColor(primaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(typeface, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        resetViewButton = TextView(this).apply {
            text = "Reset"
            minHeight = dp(GalleryGlazeContract.GENERAL_TARGET_DP)
            gravity = Gravity.CENTER
            setPadding(dp(14), 0, dp(14), 0)
            setTextColor(accentColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            background = roundedSurface(withAlpha(accentColor, 0.10f), 16)
            contentDescription = "Reset Gallery type, album, and sort controls"
            setOnClickListener {
                selectedFilter = MediaTypeFilter.ALL
                selectedSort = MediaSortOrder.NEWEST
                selectedAlbumId = null
                renderFilterControls()
                renderSortControls()
                renderAlbumControls()
                renderAuthorizedSnapshot(loadGeneration)
                announceForAccessibility("Gallery view reset to all albums, all media, newest first")
            }
        }
        libraryHeading.addView(resetViewButton)
        controlsSection.addView(libraryHeading)

        filterRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
        }
        controlsSection.addView(
            filterRow,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
        )

        sortRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
        }
        controlsSection.addView(
            sortRow,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(8)
            },
        )

        albumRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
        }
        controlsSection.addView(
            HorizontalScrollView(this).apply {
                isHorizontalScrollBarEnabled = false
                contentDescription = "Authorized album filters"
                addView(
                    albumRow,
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
                )
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(8)
            },
        )

        viewSummary = TextView(this).apply {
            setTextColor(secondaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(0, dp(10), 0, dp(10))
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        controlsSection.addView(viewSummary)
        content.addView(controlsSection)

        library = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(
            library,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
        )

        renderFilterControls()
        renderSortControls()
        renderAlbumControls()
        renderViewSummary()

        setContentView(ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        })
    }

    private fun renderFilterControls() {
        if (!::filterRow.isInitialized) return
        filterRow.removeAllViews()
        MediaTypeFilter.entries.forEachIndexed { index, filter ->
            val label = when (filter) {
                MediaTypeFilter.ALL -> "All"
                MediaTypeFilter.IMAGES -> "Photos"
                MediaTypeFilter.VIDEOS -> "Videos"
            }
            val button = chip(
                label = label,
                selected = filter == selectedFilter,
                enabled = authorizedItems.isNotEmpty(),
                description = "Show ${label.lowercase()} in the current authorized library",
            ) {
                selectedFilter = filter
                renderFilterControls()
                renderAuthorizedSnapshot(loadGeneration)
            }
            filterRow.addView(
                button,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    if (index > 0) marginStart = dp(8)
                },
            )
        }
    }

    private fun renderSortControls() {
        if (!::sortRow.isInitialized) return
        sortRow.removeAllViews()
        MediaSortOrder.entries.forEachIndexed { index, sortOrder ->
            val label = when (sortOrder) {
                MediaSortOrder.NEWEST -> "Newest"
                MediaSortOrder.OLDEST -> "Oldest"
            }
            val button = chip(
                label = label,
                selected = sortOrder == selectedSort,
                enabled = authorizedItems.isNotEmpty(),
                description = "Sort the current authorized library ${label.lowercase()} first",
            ) {
                selectedSort = sortOrder
                renderSortControls()
                renderAuthorizedSnapshot(loadGeneration)
            }
            sortRow.addView(
                button,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    if (index > 0) marginStart = dp(8)
                },
            )
        }
    }

    private fun renderAlbumControls() {
        if (!::albumRow.isInitialized) return
        val options = mediaAlbumOptions(authorizedItems)
        if (selectedAlbumId != null && options.none { it.albumId == selectedAlbumId }) selectedAlbumId = null
        albumRow.removeAllViews()

        fun addAlbumChip(albumId: String?, label: String, index: Int) {
            val button = chip(
                label = label,
                selected = albumId == selectedAlbumId,
                enabled = authorizedItems.isNotEmpty(),
                description = if (albumId == null) {
                    "Show all albums in the current authorized library"
                } else {
                    "Show album $label in the current authorized library"
                },
            ) {
                selectedAlbumId = albumId
                renderAlbumControls()
                renderAuthorizedSnapshot(loadGeneration)
            }
            albumRow.addView(
                button,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    if (index > 0) marginStart = dp(8)
                },
            )
        }

        addAlbumChip(null, "All albums", 0)
        options.forEachIndexed { index, option ->
            addAlbumChip(option.albumId, "${option.albumName} · ${option.itemCount}", index + 1)
        }
    }

    private fun renderViewSummary() {
        if (!::viewSummary.isInitialized || !::resetViewButton.isInitialized) return
        val summary = summarizeAuthorizedMediaView(authorizedItems, selectedFilter, selectedSort, selectedAlbumId)
        val typeLabel = when (summary.mediaTypeFilter) {
            MediaTypeFilter.ALL -> "All media"
            MediaTypeFilter.IMAGES -> "Photos"
            MediaTypeFilter.VIDEOS -> "Videos"
        }
        val sortLabel = when (summary.sortOrder) {
            MediaSortOrder.NEWEST -> "Newest first"
            MediaSortOrder.OLDEST -> "Oldest first"
        }
        val albumLabel = summary.albumName ?: "All albums"
        viewSummary.text = buildString {
            append("${summary.presentedCount}")
            if (summary.presentedCount != summary.authorizedCount) append(" of ${summary.authorizedCount}")
            append(" items · $albumLabel · $typeLabel · $sortLabel")
        }
        val canReset = authorizedItems.isNotEmpty() && summary.hasNonDefaultControls
        resetViewButton.isEnabled = canReset
        resetViewButton.alpha = if (canReset) 1f else 0.36f
    }

    private fun renderPermissionState() {
        val accessScope = currentMediaAccessScope()
        if (!GalleryMediaAccessPolicy.canRead(accessScope)) {
            loadGeneration += 1
            authorizedItems = emptyList()
            selectedAlbumId = null
            controlsSection.visibility = View.GONE
            status.text = "Choose which photos and videos Gallery can see. Your local media stays on this device."
            action.isEnabled = true
            action.alpha = 1f
            action.text = "Choose media"
            action.setOnClickListener { requestReadableMediaAccess() }
            renderFilterControls()
            renderSortControls()
            renderAlbumControls()
            renderViewSummary()
            library.removeAllViews()
            library.addView(
                messageRow(
                    title = "Your library is private by default",
                    message = "Gallery will only read the media Android authorizes. No cloud account or network connection is required.",
                ),
            )
            return
        }

        action.isEnabled = true
        action.alpha = 1f
        if (GalleryMediaAccessPolicy.isPartial(accessScope)) {
            action.text = "Change access"
            action.setOnClickListener { requestReadableMediaAccess() }
        } else {
            action.text = "Refresh"
            action.setOnClickListener { loadLocalLibrary(accessScope) }
        }
        loadLocalLibrary(accessScope)
    }

    private fun loadLocalLibrary(accessScope: GalleryMediaAccessScope) {
        val generation = ++loadGeneration
        action.isEnabled = false
        action.alpha = 0.45f
        controlsSection.visibility = View.GONE
        status.text = "${accessScopeLabel(accessScope)} · Loading…"
        library.removeAllViews()
        library.addView(messageRow("Loading your library", "Reading the local media Android has authorized."))

        thread(name = "goreecloud-gallery-mediastore") {
            try {
                val result = AndroidMediaStoreReader(contentResolver).readLatest(GalleryGlazeContract.MAX_RENDERED_MEDIA_ROWS)
                runOnUiThread {
                    if (generation != loadGeneration) return@runOnUiThread
                    authorizedItems = result.items
                    action.isEnabled = true
                    action.alpha = 1f
                    status.text = buildString {
                        append(accessScopeLabel(accessScope))
                        append(" · ${result.items.size} item")
                        if (result.items.size != 1) append('s')
                        if (result.rejectedRowCount > 0) append(" · ${result.rejectedRowCount} skipped")
                    }
                    renderFilterControls()
                    renderSortControls()
                    renderAlbumControls()
                    controlsSection.visibility = if (authorizedItems.isEmpty()) View.GONE else View.VISIBLE
                    renderAuthorizedSnapshot(generation)
                }
            } catch (_: SecurityException) {
                renderLoadFailure(generation, "Android denied the current local media read.")
            } catch (_: RuntimeException) {
                renderLoadFailure(generation, "The local media provider is unavailable right now.")
            }
        }
    }

    private fun renderLoadFailure(generation: Int, message: String) {
        runOnUiThread {
            if (generation != loadGeneration) return@runOnUiThread
            authorizedItems = emptyList()
            selectedAlbumId = null
            controlsSection.visibility = View.GONE
            action.isEnabled = true
            action.alpha = 1f
            action.text = "Try again"
            action.setOnClickListener {
                val scope = currentMediaAccessScope()
                if (GalleryMediaAccessPolicy.canRead(scope)) loadLocalLibrary(scope) else requestReadableMediaAccess()
            }
            status.text = message
            renderFilterControls()
            renderSortControls()
            renderAlbumControls()
            renderViewSummary()
            library.removeAllViews()
            library.addView(
                messageRow(
                    "Library unavailable",
                    "Gallery is not treating a failed provider read as an empty library. Try again or review media access.",
                ),
            )
        }
    }

    private fun renderAuthorizedSnapshot(generation: Int) {
        if (generation != loadGeneration) return
        val albumFilteredItems = filterAuthorizedAlbum(authorizedItems, selectedAlbumId)
        val presentedItems = selectedSort.sort(selectedFilter.filter(albumFilteredItems))
        renderViewSummary()
        library.removeAllViews()

        if (presentedItems.isEmpty()) {
            val typeLabel = when (selectedFilter) {
                MediaTypeFilter.ALL -> "photos or videos"
                MediaTypeFilter.IMAGES -> "photos"
                MediaTypeFilter.VIDEOS -> "videos"
            }
            val albumLabel = mediaAlbumOptions(authorizedItems).firstOrNull { it.albumId == selectedAlbumId }?.albumName
            val message = if (albumLabel == null) {
                "No authorized $typeLabel are present in this view."
            } else {
                "No authorized $typeLabel are present in $albumLabel."
            }
            library.addView(messageRow("Nothing here", message))
            return
        }

        renderMediaGrid(presentedItems, generation)
    }

    private fun renderMediaGrid(items: List<MediaItem>, generation: Int) {
        val columns = GalleryGlazeContract.gridColumns(resources.configuration.screenWidthDp)
        val gutterPx = dp(GalleryGlazeContract.horizontalGutterDp(resources.configuration.screenWidthDp))
        val gaps = dp(GRID_GAP_DP) * (columns - 1)
        val tileSize = (
            (resources.displayMetrics.widthPixels - (gutterPx * 2) - gaps) / columns
        ).coerceAtLeast(dp(GalleryGlazeContract.MIN_GRID_TILE_DP))

        items.chunked(columns).forEachIndexed { rowIndex, rowItems ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.START
            }
            rowItems.forEachIndexed { columnIndex, item ->
                val absoluteIndex = rowIndex * columns + columnIndex
                row.addView(
                    mediaTile(item, items, absoluteIndex, generation),
                    LinearLayout.LayoutParams(0, tileSize, 1f).apply {
                        if (columnIndex > 0) marginStart = dp(GRID_GAP_DP)
                    },
                )
            }
            repeat(columns - rowItems.size) { spacerIndex ->
                row.addView(
                    Space(this),
                    LinearLayout.LayoutParams(0, tileSize, 1f).apply {
                        if (rowItems.isNotEmpty() || spacerIndex > 0) marginStart = dp(GRID_GAP_DP)
                    },
                )
            }
            library.addView(
                row,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tileSize).apply {
                    if (rowIndex > 0) topMargin = dp(GRID_GAP_DP)
                },
            )
        }
    }

    private fun mediaTile(
        item: MediaItem,
        items: List<MediaItem>,
        index: Int,
        generation: Int,
    ): FrameLayout {
        val placeholder = withAlpha(themeColor(android.R.attr.textColorPrimary, 0xff1d1d1f.toInt()), 0.08f)
        val cacheKey = thumbnailCacheKey(GRID_THUMBNAIL_NAMESPACE, item.contentUri)
        val thumbnail = ImageView(this).apply {
            tag = cacheKey
            contentDescription = null
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = roundedSurface(placeholder, GRID_CORNER_DP)
        }
        loadLocalThumbnail(item, thumbnail, generation, GRID_THUMBNAIL_DP, GRID_THUMBNAIL_NAMESPACE)

        return FrameLayout(this).apply {
            background = roundedSurface(placeholder, GRID_CORNER_DP)
            clipToOutline = true
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            isClickable = true
            isFocusable = true
            contentDescription = "${item.displayName}. ${mediaMetadata(item)}. Double tap to open preview."
            setOnClickListener { showAuthorizedPreview(items, index, generation) }
            addView(
                thumbnail,
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
            )
            if (item.mimeType.startsWith("video/")) {
                addView(
                    TextView(context).apply {
                        text = "VIDEO"
                        setTextColor(Color.WHITE)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                        setTypeface(typeface, Typeface.BOLD)
                        gravity = Gravity.CENTER
                        setPadding(dp(8), dp(4), dp(8), dp(4))
                        background = roundedSurface(0xb3000000.toInt(), 10)
                        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    },
                    FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        gravity = Gravity.END or Gravity.BOTTOM
                        marginEnd = dp(8)
                        bottomMargin = dp(8)
                    },
                )
            }
        }
    }

    private fun showAuthorizedPreview(items: List<MediaItem>, initialIndex: Int, generation: Int) {
        if (
            generation != loadGeneration ||
            !GalleryMediaAccessPolicy.canRead(currentMediaAccessScope()) ||
            initialIndex !in items.indices
        ) return

        val primaryTextColor = themeColor(android.R.attr.textColorPrimary, 0xff1d1d1f.toInt())
        val secondaryTextColor = themeColor(android.R.attr.textColorSecondary, 0xff666666.toInt())
        val preview = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            minimumHeight = dp(VIEWER_PREVIEW_DP)
            background = roundedSurface(
                withAlpha(themeColor(android.R.attr.textColorPrimary, 0xff1d1d1f.toInt()), 0.06f),
                18,
            )
        }
        val metadata = TextView(this).apply {
            setTextColor(primaryTextColor)
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
                if (
                    generation != loadGeneration ||
                    !GalleryMediaAccessPolicy.canRead(currentMediaAccessScope()) ||
                    currentIndex !in items.indices
                ) {
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
                    "Video poster preview only. Playback remains a separate Development milestone."
                } else {
                    "Local preview from the authorized item. Full-resolution editing remains a separate Development milestone."
                }
                previousButton?.isEnabled = currentIndex > 0
                nextButton?.isEnabled = currentIndex < items.lastIndex
                loadLocalThumbnail(item, preview, generation, VIEWER_PREVIEW_DP, VIEWER_THUMBNAIL_NAMESPACE)
            }

            previousButton?.setOnClickListener {
                if (currentIndex > 0) {
                    currentIndex -= 1
                    renderCurrentItem()
                }
            }
            nextButton?.setOnClickListener {
                if (currentIndex < items.lastIndex) {
                    currentIndex += 1
                    renderCurrentItem()
                }
            }
            renderCurrentItem()
        }
        dialog.show()
    }

    private fun chip(
        label: String,
        selected: Boolean,
        enabled: Boolean,
        description: String,
        onClick: () -> Unit,
    ): TextView {
        val primaryTextColor = themeColor(android.R.attr.textColorPrimary, 0xff1d1d1f.toInt())
        val accentColor = themeColor(android.R.attr.colorAccent, 0xff2e7d6f.toInt())
        val neutral = withAlpha(primaryTextColor, 0.06f)
        val selectedSurface = withAlpha(accentColor, 0.16f)
        return TextView(this).apply {
            text = label
            minHeight = dp(GalleryGlazeContract.GENERAL_TARGET_DP)
            gravity = Gravity.CENTER
            setPadding(dp(14), 0, dp(14), 0)
            setTextColor(if (selected) accentColor else primaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            if (selected) setTypeface(typeface, Typeface.BOLD)
            background = roundedSurface(if (selected) selectedSurface else neutral, 16)
            isEnabled = enabled
            isClickable = enabled
            isFocusable = enabled
            alpha = if (enabled) 1f else 0.38f
            contentDescription = description
            setOnClickListener { onClick() }
        }
    }

    private fun mediaMetadata(item: MediaItem): String {
        val timestamp = item.capturedAt ?: item.modifiedAt
        val kind = if (item.mimeType.startsWith("video/")) "Video" else "Photo"
        return listOfNotNull(
            kind,
            item.albumName?.let { "Album: $it" },
            DATE_TIME_FORMAT.format(timestamp),
            formatBytes(item.sizeBytes),
        ).joinToString(" · ")
    }

    private fun loadLocalThumbnail(
        item: MediaItem,
        target: ImageView,
        generation: Int,
        sizeDp: Int,
        namespace: String,
    ) {
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
            runOnUiThread {
                if (generation == loadGeneration && target.tag == cacheKey) target.setImageBitmap(bitmap)
            }
        }
    }

    private fun thumbnailCacheKey(namespace: String, contentUri: String): String = "$namespace:$contentUri"

    private fun messageRow(title: String, message: String): LinearLayout {
        val primaryTextColor = themeColor(android.R.attr.textColorPrimary, 0xff1d1d1f.toInt())
        val secondaryTextColor = themeColor(android.R.attr.textColorSecondary, 0xff666666.toInt())
        val surface = withAlpha(primaryTextColor, 0.05f)
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(28), dp(20), dp(28))
            background = roundedSurface(surface, 22)
            addView(TextView(context).apply {
                text = title
                gravity = Gravity.CENTER
                setTextColor(primaryTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
                setTypeface(typeface, Typeface.BOLD)
            })
            addView(TextView(context).apply {
                text = message
                gravity = Gravity.CENTER
                setTextColor(secondaryTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setLineSpacing(0f, 1.10f)
                setPadding(0, dp(8), 0, 0)
            })
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(20)
            }
        }
    }

    private fun currentMediaAccessScope(): GalleryMediaAccessScope {
        fun granted(permission: String) = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        return GalleryMediaAccessPolicy.resolve(
            GalleryMediaPermissionSnapshot(
                apiLevel = Build.VERSION.SDK_INT,
                readExternalStorage = Build.VERSION.SDK_INT <= 32 && granted(Manifest.permission.READ_EXTERNAL_STORAGE),
                readMediaImages = Build.VERSION.SDK_INT >= 33 && granted(Manifest.permission.READ_MEDIA_IMAGES),
                readMediaVideo = Build.VERSION.SDK_INT >= 33 && granted(Manifest.permission.READ_MEDIA_VIDEO),
                readMediaVisualUserSelected =
                    Build.VERSION.SDK_INT >= 34 && granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED),
            ),
        )
    }

    private fun accessScopeLabel(scope: GalleryMediaAccessScope): String = when (scope) {
        GalleryMediaAccessScope.DENIED -> "Media access denied"
        GalleryMediaAccessScope.LEGACY_FULL -> "Local media access"
        GalleryMediaAccessScope.SELECTED -> "Selected media only"
        GalleryMediaAccessScope.IMAGES -> "Photos authorized"
        GalleryMediaAccessScope.VIDEOS -> "Videos authorized"
        GalleryMediaAccessScope.IMAGES_AND_VIDEOS -> "Photos and videos authorized"
    }

    private fun requestReadableMediaAccess() {
        val permissions = when {
            Build.VERSION.SDK_INT >= 34 -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            )
            Build.VERSION.SDK_INT >= 33 ->
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissions(permissions, MEDIA_PERMISSION_REQUEST)
    }

    private fun roundedSurface(color: Int, radiusDp: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun withAlpha(color: Int, alpha: Float): Int = Color.argb(
        (255f * alpha.coerceIn(0f, 1f)).toInt(),
        Color.red(color),
        Color.green(color),
        Color.blue(color),
    )

    private fun themeColor(attribute: Int, fallback: Int): Int {
        val attributes = obtainStyledAttributes(intArrayOf(attribute))
        return try {
            attributes.getColor(0, fallback)
        } finally {
            attributes.recycle()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private companion object {
        const val MEDIA_PERMISSION_REQUEST = 4101
        const val GRID_GAP_DP = 3
        const val GRID_CORNER_DP = 12
        const val GRID_THUMBNAIL_DP = 192
        const val VIEWER_PREVIEW_DP = 320
        const val THUMBNAIL_WORKERS = 2
        const val THUMBNAIL_CACHE_KIB = 8 * 1024
        const val GRID_THUMBNAIL_NAMESPACE = "grid"
        const val VIEWER_THUMBNAIL_NAMESPACE = "viewer"

        val DATE_TIME_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a").withZone(ZoneId.systemDefault())

        fun formatBytes(bytes: Long): String = when {
            bytes >= 1024L * 1024L -> String.format("%.1f MiB", bytes / (1024.0 * 1024.0))
            bytes >= 1024L -> String.format("%.1f KiB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
