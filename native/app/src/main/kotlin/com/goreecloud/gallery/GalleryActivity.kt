package com.goreecloud.gallery

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.LruCache
import android.util.Size
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import com.goreecloud.gallery.android.AndroidMediaStoreReader
import com.goreecloud.gallery.core.MediaItem
import com.goreecloud.gallery.core.MediaSortOrder
import com.goreecloud.gallery.core.buildAlbumCatalog
import com.goreecloud.gallery.core.sort
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class GalleryActivity : Activity() {
    private lateinit var rootFrame: FrameLayout
    private lateinit var content: LinearLayout
    private lateinit var headerTitle: TextView
    private lateinit var headerSubtitle: TextView
    private lateinit var backControl: ImageView
    private lateinit var searchControl: ImageView
    private lateinit var sortControl: ImageView
    private lateinit var searchContainer: LinearLayout
    private lateinit var searchField: EditText
    private lateinit var accessPanel: LinearLayout
    private lateinit var status: TextView
    private lateinit var action: TextView
    private lateinit var library: LinearLayout
    private lateinit var navigationCapsule: LinearLayout

    private var thumbnailWorkerCount = GalleryFileLoadingPriority.FAST.thumbnailWorkerCount
    private var thumbnailExecutor: ExecutorService = Executors.newFixedThreadPool(thumbnailWorkerCount)
    private val thumbnailCache = object : LruCache<String, Bitmap>(THUMBNAIL_CACHE_KIB) {
        override fun sizeOf(key: String, value: Bitmap): Int = maxOf(1, value.allocationByteCount / 1024)
    }

    private val favoriteUris = linkedSetOf<String>()
    private var loadGeneration = 0
    private var authorizedItems: List<MediaItem> = emptyList()
    private var selectedSort = MediaSortOrder.NEWEST
    private var destination = GalleryDestination.PHOTOS
    private var openAlbumId: String? = null
    private var showingFavorites = false
    private var searchQuery = ""
    private var viewerOverlay: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favoriteUris += galleryPreferences()
            .getStringSet(FAVORITES_KEY, emptySet())
            .orEmpty()
        reconfigureThumbnailExecutor(currentUserSettings().fileLoadingPriority)
        buildSurface()
    }

    override fun onResume() {
        super.onResume()
        if (viewerOverlay != null) return
        if (destination == GalleryDestination.SETTINGS) {
            renderCurrentDestination()
        } else {
            renderPermissionState()
        }
    }

    @Deprecated("The Development Gallery uses Android document intents for local import/export portability.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        when (requestCode) {
            EXPORT_FAVORITES_REQUEST -> writeJsonDocument(
                uri = uri,
                json = buildFavoritesExportJson(),
                successMessage = "Favorites exported",
            )
            IMPORT_FAVORITES_REQUEST -> readJsonDocument(uri) { importFavorites(it) }
            EXPORT_SETTINGS_REQUEST -> writeJsonDocument(
                uri = uri,
                json = buildSettingsExportJson(),
                successMessage = "Gallery settings exported",
            )
            IMPORT_SETTINGS_REQUEST -> readJsonDocument(uri) { importSettings(it) }
        }
    }

    override fun onDestroy() {
        thumbnailExecutor.shutdownNow()
        thumbnailCache.evictAll()
        super.onDestroy()
    }

    @Deprecated("Activity back navigation is intentionally handled for the native Development shell.")
    override fun onBackPressed() {
        if (viewerOverlay != null) {
            closeAuthorizedViewer()
            return
        }
        if (destination == GalleryDestination.ALBUMS && (openAlbumId != null || showingFavorites)) {
            openAlbumId = null
            showingFavorites = false
            renderCurrentDestination()
            return
        }
        super.onBackPressed()
    }

    private fun buildSurface() {
        rootFrame = FrameLayout(this).apply {
            setBackgroundColor(canvasColor())
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(GalleryGlazeContract.horizontalGutterDp(resources.configuration.screenWidthDp)),
                dp(12),
                dp(GalleryGlazeContract.horizontalGutterDp(resources.configuration.screenWidthDp)),
                dp(GalleryGlazeContract.CONTENT_BOTTOM_INSET_DP),
            )
            setBackgroundColor(canvasColor())
        }

        content.addView(buildHeader())
        content.addView(buildSearchSurface())
        content.addView(buildAccessPanel())

        library = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, 0)
        }
        content.addView(
            library,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
        )

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            addView(
                content,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
            )
        }
        rootFrame.addView(
            scroll,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).apply {
                bottomMargin = dp(GalleryGlazeContract.NAVIGATION_RESERVED_SPACE_DP)
            },
        )

        navigationCapsule = buildNavigationCapsule()
        rootFrame.addView(
            navigationCapsule,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(GalleryGlazeContract.NAVIGATION_HEIGHT_DP)).apply {
                gravity = Gravity.BOTTOM
                marginStart = dp(GalleryGlazeContract.NAVIGATION_SIDE_MARGIN_DP)
                marginEnd = dp(GalleryGlazeContract.NAVIGATION_SIDE_MARGIN_DP)
                bottomMargin = dp(GalleryGlazeContract.NAVIGATION_BOTTOM_MARGIN_DP)
            },
        )

        setContentView(rootFrame)
        applySystemChrome()
        renderNavigation()
        updateHeader()
    }

    private fun buildHeader(): View {
        val primaryTextColor = primaryTextColor()
        val secondaryTextColor = secondaryTextColor()

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(3), 0, 0)
        }

        backControl = iconHeaderAction(R.drawable.ic_gallery_back, "Back to Albums") {
            openAlbumId = null
            showingFavorites = false
            renderCurrentDestination()
        }.apply {
            visibility = View.GONE
        }
        row.addView(
            backControl,
            LinearLayout.LayoutParams(
                dp(GalleryGlazeContract.GENERAL_TARGET_DP),
                dp(GalleryGlazeContract.GENERAL_TARGET_DP),
            ),
        )

        val titles = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        headerTitle = TextView(this).apply {
            setTextColor(primaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
            setTypeface(typeface, Typeface.BOLD)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        headerSubtitle = TextView(this).apply {
            setTextColor(secondaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
            setPadding(0, dp(1), 0, 0)
        }
        titles.addView(headerTitle)
        titles.addView(headerSubtitle)
        row.addView(
            titles,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(2)
            },
        )

        searchControl = iconHeaderAction(
            R.drawable.ic_gallery_search,
            "Search the current Gallery destination",
        ) {
            toggleSearch()
        }
        row.addView(
            searchControl,
            LinearLayout.LayoutParams(
                dp(GalleryGlazeContract.GENERAL_TARGET_DP),
                dp(GalleryGlazeContract.GENERAL_TARGET_DP),
            ),
        )

        sortControl = iconHeaderAction(
            R.drawable.ic_gallery_sort,
            "Change Gallery sort order",
        ) {
            selectedSort = if (selectedSort == MediaSortOrder.NEWEST) MediaSortOrder.OLDEST else MediaSortOrder.NEWEST
            renderCurrentDestination()
            announceForAccessibility(
                if (selectedSort == MediaSortOrder.NEWEST) "Sorted newest first" else "Sorted oldest first",
            )
        }
        row.addView(
            sortControl,
            LinearLayout.LayoutParams(
                dp(GalleryGlazeContract.GENERAL_TARGET_DP),
                dp(GalleryGlazeContract.GENERAL_TARGET_DP),
            ).apply {
                marginStart = dp(4)
            },
        )

        return row
    }

    private fun buildSearchSurface(): View {
        searchContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
            setPadding(dp(14), dp(2), dp(4), dp(2))
            background = roundedSurface(
                withAlpha(primaryTextColor(), if (isNightMode()) 0.12f else 0.055f),
                18,
            )
        }

        searchField = EditText(this).apply {
            hint = "Search photos, videos, and albums"
            setSingleLine(true)
            setTextColor(primaryTextColor())
            setHintTextColor(withAlpha(secondaryTextColor(), 0.88f))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            background = null
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    searchQuery = s?.toString()?.trim().orEmpty()
                    renderCurrentDestination()
                }
                override fun afterTextChanged(s: Editable?) = Unit
            })
        }
        searchContainer.addView(
            searchField,
            LinearLayout.LayoutParams(0, dp(GalleryGlazeContract.GENERAL_TARGET_DP), 1f),
        )

        searchContainer.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.ic_gallery_close)
                setColorFilter(primaryTextColor())
                setPadding(dp(13), dp(13), dp(13), dp(13))
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                isClickable = true
                isFocusable = true
                contentDescription = "Close search"
                background = roundedSurface(Color.TRANSPARENT, 16)
                setOnClickListener { closeSearch() }
            },
            LinearLayout.LayoutParams(
                dp(GalleryGlazeContract.GENERAL_TARGET_DP),
                dp(GalleryGlazeContract.GENERAL_TARGET_DP),
            ),
        )

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, 0)
            addView(
                searchContainer,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
            )
        }
    }

    private fun buildAccessPanel(): View {
        accessPanel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
            setPadding(dp(12), dp(8), dp(6), dp(8))
            background = roundedSurface(
                withAlpha(primaryTextColor(), if (isNightMode()) 0.12f else 0.05f),
                16,
            )
        }

        status = TextView(this).apply {
            setTextColor(primaryTextColor())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
            setLineSpacing(0f, 1.06f)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        accessPanel.addView(
            status,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(8)
            },
        )

        action = TextView(this).apply {
            minHeight = dp(GalleryGlazeContract.GENERAL_TARGET_DP)
            gravity = Gravity.CENTER
            setPadding(dp(12), 0, dp(12), 0)
            setTextColor(accentColor())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
            setTypeface(typeface, Typeface.BOLD)
            background = roundedSurface(withAlpha(accentColor(), 0.12f), 15)
            isClickable = true
            isFocusable = true
            contentDescription = "Gallery media access action"
        }
        accessPanel.addView(action)

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, 0)
            addView(
                accessPanel,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
            )
        }
    }

    private fun buildNavigationCapsule(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(3), dp(3), dp(3), dp(3))
            background = roundedSurface(
                if (isNightMode()) 0xf21d1d1f.toInt() else 0xf2ffffff.toInt(),
                GalleryGlazeContract.NAVIGATION_RADIUS_DP,
            )
            elevation = dp(GalleryGlazeContract.NAVIGATION_ELEVATION_DP).toFloat()
        }
    }

    private fun renderNavigation() {
        if (!::navigationCapsule.isInitialized) return
        navigationCapsule.removeAllViews()
        GalleryDestination.entries.forEachIndexed { index, item ->
            val selected = destination == item
            val label = when (item) {
                GalleryDestination.PHOTOS -> "Photos"
                GalleryDestination.ALBUMS -> "Albums"
                GalleryDestination.VIDEOS -> "Videos"
                GalleryDestination.SETTINGS -> "Settings"
            }
            navigationCapsule.addView(
                TextView(this).apply {
                    text = label
                    gravity = Gravity.CENTER
                    minHeight = dp(GalleryGlazeContract.GENERAL_TARGET_DP)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                    setTextColor(if (selected) accentColor() else primaryTextColor())
                    if (selected) setTypeface(typeface, Typeface.BOLD)
                    background = roundedSurface(
                        if (selected) withAlpha(accentColor(), 0.13f) else Color.TRANSPARENT,
                        18,
                    )
                    isClickable = true
                    isFocusable = true
                    isSelected = selected
                    contentDescription = "$label${if (selected) ", selected" else ""}"
                    setOnClickListener {
                        if (destination == item && openAlbumId == null && !showingFavorites) return@setOnClickListener
                        destination = item
                        openAlbumId = null
                        showingFavorites = false
                        searchQuery = ""
                        if (::searchField.isInitialized) searchField.setText("")
                        closeSearch(clearQuery = false)
                        renderNavigation()
                        renderCurrentDestination()
                    }
                },
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                    if (index > 0) marginStart = dp(2)
                },
            )
        }
    }

    private fun updateHeader() {
        if (!::headerTitle.isInitialized) return
        val visibleItems = visibleAuthorizedItems()

        val collectionItems = when {
            destination == GalleryDestination.ALBUMS && showingFavorites ->
                visibleItems.filter { it.contentUri in favoriteUris }
            destination == GalleryDestination.ALBUMS && openAlbumId != null ->
                visibleItems.filter { it.albumId == openAlbumId }
            else -> emptyList()
        }

        val title = when {
            destination == GalleryDestination.ALBUMS && showingFavorites -> "Favorites"
            destination == GalleryDestination.ALBUMS && openAlbumId != null ->
                visibleItems.firstOrNull { it.albumId == openAlbumId }?.albumName
                    ?: authorizedItems.firstOrNull { it.albumId == openAlbumId }?.albumName
                    ?: "Album"
            destination == GalleryDestination.PHOTOS -> "Photos"
            destination == GalleryDestination.ALBUMS -> "Albums"
            destination == GalleryDestination.VIDEOS -> "Videos"
            destination == GalleryDestination.SETTINGS -> "Settings"
            else -> "Gallery"
        }

        val baseSubtitle = when {
            destination == GalleryDestination.SETTINGS -> "Local Gallery preferences"
            destination == GalleryDestination.ALBUMS && (showingFavorites || openAlbumId != null) ->
                itemCountLabel(collectionItems.size)
            destination == GalleryDestination.PHOTOS ->
                itemCountLabel(visibleItems.count { it.mimeType.startsWith("image/") })
            destination == GalleryDestination.VIDEOS ->
                itemCountLabel(visibleItems.count { it.mimeType.startsWith("video/") })
            destination == GalleryDestination.ALBUMS -> {
                val albumCount = visibleItems.buildAlbumCatalog().size
                val favoriteSuffix = if (favoriteUris.any { uri -> visibleItems.any { it.contentUri == uri } }) 1 else 0
                val totalCollections = albumCount + favoriteSuffix
                if (totalCollections == 1) "1 collection" else "$totalCollections collections"
            }
            else -> ""
        }

        headerTitle.text = title
        headerSubtitle.text = when {
            destination == GalleryDestination.SETTINGS -> baseSubtitle
            visibleItems.isEmpty() -> baseSubtitle
            else -> "$baseSubtitle · ${sortOrderLabel()}"
        }
        backControl.visibility =
            if (destination == GalleryDestination.ALBUMS && (openAlbumId != null || showingFavorites)) View.VISIBLE
            else View.GONE
        sortControl.contentDescription = "Sort order: ${sortOrderLabel()}. Double tap to change."
        val showMediaControls = destination != GalleryDestination.SETTINGS && visibleItems.isNotEmpty()
        sortControl.visibility = if (showMediaControls) View.VISIBLE else View.GONE
        searchControl.visibility = if (showMediaControls) View.VISIBLE else View.GONE
    }

    private fun renderPermissionState() {
        if (destination == GalleryDestination.SETTINGS) {
            accessPanel.visibility = View.GONE
            renderCurrentDestination()
            return
        }

        val accessScope = currentMediaAccessScope()
        if (!GalleryMediaAccessPolicy.canRead(accessScope)) {
            loadGeneration += 1
            authorizedItems = emptyList()
            openAlbumId = null
            showingFavorites = false
            accessPanel.visibility = View.VISIBLE
            status.text = "Choose which photos and videos Gallery can see. Your local media stays on this device."
            action.isEnabled = true
            action.alpha = 1f
            action.text = "Choose media"
            action.setOnClickListener { requestReadableMediaAccess() }
            library.removeAllViews()
            library.addView(
                emptyState(
                    title = "Your library stays private",
                    message = "Gallery only reads media Android authorizes. No GoreeCloud account or network connection is required.",
                ),
            )
            updateHeader()
            return
        }

        action.isEnabled = true
        action.alpha = 1f
        if (GalleryMediaAccessPolicy.isPartial(accessScope)) {
            accessPanel.visibility = View.VISIBLE
            status.text = "${accessScopeLabel(accessScope)}. Gallery only shows the media Android currently authorizes."
            action.text = "Change access"
            action.setOnClickListener { requestReadableMediaAccess() }
        } else {
            accessPanel.visibility = View.GONE
            action.text = "Refresh"
            action.setOnClickListener { loadLocalLibrary(accessScope) }
        }
        loadLocalLibrary(accessScope)
    }

    private fun loadLocalLibrary(accessScope: GalleryMediaAccessScope) {
        val generation = ++loadGeneration
        accessPanel.visibility = View.VISIBLE
        action.isEnabled = false
        action.alpha = 0.45f
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

                    if (GalleryMediaAccessPolicy.isPartial(accessScope)) {
                        accessPanel.visibility = View.VISIBLE
                        status.text = buildString {
                            append(accessScopeLabel(accessScope))
                            append(" · ${result.items.size} item")
                            if (result.items.size != 1) append('s')
                            if (result.rejectedRowCount > 0) append(" · ${result.rejectedRowCount} skipped")
                        }
                        action.text = "Change access"
                        action.setOnClickListener { requestReadableMediaAccess() }
                    } else {
                        accessPanel.visibility = View.GONE
                    }

                    renderCurrentDestination(generation)
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
            openAlbumId = null
            showingFavorites = false
            accessPanel.visibility = View.VISIBLE
            action.isEnabled = true
            action.alpha = 1f
            action.text = "Try again"
            action.setOnClickListener {
                val scope = currentMediaAccessScope()
                if (GalleryMediaAccessPolicy.canRead(scope)) loadLocalLibrary(scope) else requestReadableMediaAccess()
            }
            status.text = message
            library.removeAllViews()
            library.addView(
                messageRow(
                    "Library unavailable",
                    "Gallery is not treating a failed provider read as an empty library. Try again or review media access.",
                ),
            )
            updateHeader()
        }
    }

    private fun renderCurrentDestination(generation: Int = loadGeneration) {
        if (generation != loadGeneration) return

        updateHeader()
        renderNavigation()
        library.removeAllViews()

        if (destination == GalleryDestination.SETTINGS) {
            accessPanel.visibility = View.GONE
            if (searchContainer.visibility == View.VISIBLE) closeSearch(clearQuery = false)
            renderSettings()
            return
        }

        if (!GalleryMediaAccessPolicy.canRead(currentMediaAccessScope())) {
            renderPermissionState()
            return
        }
        val visibleItems = visibleAuthorizedItems()

        when (destination) {
            GalleryDestination.PHOTOS -> {
                val items = selectedSort.sort(
                    visibleItems
                        .filter { it.mimeType.startsWith("image/") }
                        .filter(::matchesSearch),
                )
                renderChronologicalLibrary(
                    items = items,
                    generation = generation,
                    emptyTitle = if (searchQuery.isBlank()) "No photos yet" else "No photo results",
                    emptyMessage = if (searchQuery.isBlank()) {
                        if (authorizedItems.any { it.mimeType.startsWith("image/") }) {
                            "No photos match the current folder or hidden-item visibility settings."
                        } else {
                            "No authorized photos are available in this library."
                        }
                    } else {
                        "No visible authorized photos match “$searchQuery”."
                    },
                )
            }
            GalleryDestination.VIDEOS -> {
                val items = selectedSort.sort(
                    visibleItems
                        .filter { it.mimeType.startsWith("video/") }
                        .filter(::matchesSearch),
                )
                renderChronologicalLibrary(
                    items = items,
                    generation = generation,
                    emptyTitle = if (searchQuery.isBlank()) "No videos yet" else "No video results",
                    emptyMessage = if (searchQuery.isBlank()) {
                        if (authorizedItems.any { it.mimeType.startsWith("video/") }) {
                            "No videos match the current folder or hidden-item visibility settings."
                        } else {
                            "No authorized videos are available in this library."
                        }
                    } else {
                        "No visible authorized videos match “$searchQuery”."
                    },
                )
            }
            GalleryDestination.ALBUMS -> when {
                showingFavorites -> {
                    val items = selectedSort.sort(
                        visibleItems
                            .filter { it.contentUri in favoriteUris }
                            .filter(::matchesSearch),
                    )
                    renderChronologicalLibrary(
                        items = items,
                        generation = generation,
                        emptyTitle = if (searchQuery.isBlank()) "No favorites yet" else "No favorite results",
                        emptyMessage = if (searchQuery.isBlank()) {
                            "Mark a visible photo or video as a favorite from the viewer to keep it here."
                        } else {
                            "No visible authorized favorites match “$searchQuery”."
                        },
                    )
                }
                openAlbumId != null -> {
                    val albumId = openAlbumId
                    val items = selectedSort.sort(
                        visibleItems
                            .filter { it.albumId == albumId }
                            .filter(::matchesSearch),
                    )
                    renderChronologicalLibrary(
                        items = items,
                        generation = generation,
                        emptyTitle = if (searchQuery.isBlank()) "Album is empty" else "No album results",
                        emptyMessage = if (searchQuery.isBlank()) {
                            "No visible authorized media is currently available in this album."
                        } else {
                            "No visible authorized items in this album match “$searchQuery”."
                        },
                    )
                }
                else -> renderAlbums(generation, visibleItems)
            }
            GalleryDestination.SETTINGS -> Unit
        }
    }

    private fun renderChronologicalLibrary(
        items: List<MediaItem>,
        generation: Int,
        emptyTitle: String,
        emptyMessage: String,
    ) {
        if (items.isEmpty()) {
            library.addView(emptyState(emptyTitle, emptyMessage))
            return
        }

        val groups = linkedMapOf<String, MutableList<MediaItem>>()
        items.forEach { item ->
            groups.getOrPut(dateGroupLabel(item)) { mutableListOf() }.add(item)
        }

        groups.forEach { (label, groupItems) ->
            library.addView(sectionHeader(label))
            renderMediaGrid(groupItems, generation, library)
        }
    }

    private fun renderAlbums(generation: Int, sourceItems: List<MediaItem>) {
        val query = searchQuery.lowercase()
        val catalog = sourceItems.buildAlbumCatalog()
            .let { albums ->
                if (selectedSort == MediaSortOrder.NEWEST) albums else albums.sortedBy { it.newestAt }
            }
            .filter { query.isBlank() || it.displayName.lowercase().contains(query) }

        val favoriteItems = sourceItems
            .filter { it.contentUri in favoriteUris }
            .let(selectedSort::sort)

        val showFavoritesTile = favoriteItems.isNotEmpty() &&
            (query.isBlank() || "favorites".contains(query))

        if (catalog.isEmpty() && !showFavoritesTile) {
            library.addView(
                emptyState(
                    if (searchQuery.isBlank()) "No albums yet" else "No album results",
                    if (searchQuery.isBlank()) {
                        "No visible authorized albums are available with the current Gallery settings."
                    } else {
                        "No visible authorized albums match “$searchQuery”."
                    },
                ),
            )
            return
        }

        library.addView(sectionHeader("Collections"))

        val tiles = mutableListOf<AlbumPresentation>()
        if (showFavoritesTile) {
            tiles += AlbumPresentation(
                id = null,
                name = "Favorites",
                count = favoriteItems.size,
                cover = favoriteItems.first(),
                isFavorites = true,
            )
        }
        catalog.forEach { album ->
            val cover = sourceItems.firstOrNull { it.id == album.coverItemId } ?: return@forEach
            tiles += AlbumPresentation(
                id = album.id,
                name = album.displayName,
                count = album.itemCount,
                cover = cover,
                isFavorites = false,
            )
        }

        renderAlbumGrid(tiles, generation)
    }

    private fun renderAlbumGrid(albums: List<AlbumPresentation>, generation: Int) {
        val columns = GalleryGlazeContract.albumGridColumns(resources.configuration.screenWidthDp)
        val gutterPx = dp(GalleryGlazeContract.horizontalGutterDp(resources.configuration.screenWidthDp))
        val gaps = dp(ALBUM_GAP_DP) * (columns - 1)
        val tileWidth = (
            (resources.displayMetrics.widthPixels - (gutterPx * 2) - gaps) / columns
        ).coerceAtLeast(dp(GalleryGlazeContract.MIN_ALBUM_TILE_DP))

        albums.chunked(columns).forEachIndexed { rowIndex, rowAlbums ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.START
            }

            rowAlbums.forEachIndexed { columnIndex, album ->
                row.addView(
                    albumTile(album, generation, tileWidth),
                    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                        if (columnIndex > 0) marginStart = dp(ALBUM_GAP_DP)
                    },
                )
            }
            repeat(columns - rowAlbums.size) { spacerIndex ->
                row.addView(
                    Space(this),
                    LinearLayout.LayoutParams(0, 1, 1f).apply {
                        if (rowAlbums.isNotEmpty() || spacerIndex > 0) marginStart = dp(ALBUM_GAP_DP)
                    },
                )
            }
            library.addView(
                row,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    if (rowIndex > 0) topMargin = dp(16)
                },
            )
        }
    }

    private fun albumTile(album: AlbumPresentation, generation: Int, tileWidth: Int): LinearLayout {
        val cornerDp = thumbnailCornerDp(ALBUM_CORNER_DP)
        val image = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = roundedSurface(withAlpha(primaryTextColor(), 0.08f), cornerDp)
            clipToOutline = true
            tag = thumbnailCacheKey(ALBUM_THUMBNAIL_NAMESPACE, album.cover.contentUri)
        }
        loadLocalThumbnail(album.cover, image, generation, ALBUM_THUMBNAIL_DP, ALBUM_THUMBNAIL_NAMESPACE)

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            isClickable = true
            isFocusable = true
            contentDescription = "${album.name}, ${itemCountLabel(album.count)}"
            setOnClickListener {
                searchQuery = ""
                searchField.setText("")
                closeSearch(clearQuery = false)
                if (album.isFavorites) {
                    showingFavorites = true
                    openAlbumId = null
                } else {
                    showingFavorites = false
                    openAlbumId = album.id
                }
                renderCurrentDestination()
            }
            addView(
                image,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tileWidth),
            )
            addView(TextView(context).apply {
                text = album.name
                setTextColor(primaryTextColor())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f)
                setTypeface(typeface, Typeface.BOLD)
                maxLines = 1
                setPadding(dp(2), dp(7), dp(2), 0)
            })
            addView(TextView(context).apply {
                text = itemCountLabel(album.count)
                setTextColor(secondaryTextColor())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                maxLines = 1
                setPadding(dp(2), dp(1), dp(2), 0)
            })
        }
    }

    private fun renderMediaGrid(items: List<MediaItem>, generation: Int, parent: LinearLayout) {
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
            parent.addView(
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
        val placeholder = withAlpha(primaryTextColor(), 0.08f)
        val cacheKey = thumbnailCacheKey(GRID_THUMBNAIL_NAMESPACE, item.contentUri)
        val cornerDp = thumbnailCornerDp(GRID_CORNER_DP)
        val thumbnail = ImageView(this).apply {
            tag = cacheKey
            contentDescription = null
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = roundedSurface(placeholder, cornerDp)
        }
        loadLocalThumbnail(item, thumbnail, generation, GRID_THUMBNAIL_DP, GRID_THUMBNAIL_NAMESPACE)

        return FrameLayout(this).apply {
            background = roundedSurface(placeholder, cornerDp)
            clipToOutline = true
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            isClickable = true
            isFocusable = true
            contentDescription = "${item.displayName}. ${mediaMetadata(item)}. Double tap to open viewer."
            setOnClickListener { showAuthorizedViewer(items, index, generation) }
            addView(
                thumbnail,
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
            )
            if (item.mimeType.startsWith("video/")) {
                addView(
                    TextView(context).apply {
                        text = formatVideoBadge(item)
                        setTextColor(Color.WHITE)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                        setTypeface(typeface, Typeface.BOLD)
                        gravity = Gravity.CENTER
                        setPadding(dp(7), dp(3), dp(7), dp(3))
                        background = roundedSurface(0xb3000000.toInt(), 9)
                        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    },
                    FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        gravity = Gravity.END or Gravity.BOTTOM
                        marginEnd = dp(5)
                        bottomMargin = dp(5)
                    },
                )
            }
        }
    }

    private fun showAuthorizedViewer(items: List<MediaItem>, initialIndex: Int, generation: Int) {
        if (
            generation != loadGeneration ||
            !GalleryMediaAccessPolicy.canRead(currentMediaAccessScope()) ||
            initialIndex !in items.indices
        ) return

        viewerOverlay?.let { rootFrame.removeView(it) }

        val overlay = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        viewerOverlay = overlay
        rootFrame.addView(
            overlay,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        @Suppress("DEPRECATION")
        run { window.decorView.systemUiVisibility = 0 }

        val preview = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
        }
        overlay.addView(
            preview,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).apply {
                topMargin = dp(72)
                bottomMargin = dp(104)
            },
        )

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = roundedSurface(0xd9141416.toInt(), 22)
        }
        val viewerBack = viewerAction("‹", true, "Close viewer") { closeAuthorizedViewer() }.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
        }
        topBar.addView(
            viewerBack,
            LinearLayout.LayoutParams(dp(GalleryGlazeContract.GENERAL_TARGET_DP), dp(GalleryGlazeContract.GENERAL_TARGET_DP)),
        )
        val viewerTitle = TextView(this).apply {
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 1
        }
        val viewerSubtitle = TextView(this).apply {
            setTextColor(0xffc8c8cc.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            maxLines = 1
        }
        val viewerTitles = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, dp(6), 0)
            addView(viewerTitle)
            addView(viewerSubtitle)
        }
        topBar.addView(viewerTitles, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        overlay.addView(
            topBar,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64)).apply {
                gravity = Gravity.TOP
                marginStart = dp(10)
                marginEnd = dp(10)
                topMargin = dp(6)
            },
        )

        val previous = viewerAction("‹", true, "Previous media") {}
        val next = viewerAction("›", true, "Next media") {}
        overlay.addView(
            previous,
            FrameLayout.LayoutParams(dp(52), dp(64)).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                marginStart = dp(10)
            },
        )
        overlay.addView(
            next,
            FrameLayout.LayoutParams(dp(52), dp(64)).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                marginEnd = dp(10)
            },
        )

        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(6), dp(6), dp(6), dp(6))
            background = roundedSurface(0xe8141416.toInt(), 24)
        }

        val share = viewerAction("Share", true, "Share this media") {}
        val favorite = viewerAction("Favorite", true, "Favorite this media") {}
        val edit = viewerAction("Edit", false, "Edit is unavailable in this Development build") {}
        val delete = viewerAction("Delete", false, "Delete is unavailable in this Development build") {}
        val more = viewerAction("More", true, "Show media details") {}

        listOf(share, favorite, edit, delete, more).forEachIndexed { index, item ->
            bottomBar.addView(
                item,
                LinearLayout.LayoutParams(0, dp(60), 1f).apply {
                    if (index > 0) marginStart = dp(3)
                },
            )
        }
        overlay.addView(
            bottomBar,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(72)).apply {
                gravity = Gravity.BOTTOM
                marginStart = dp(10)
                marginEnd = dp(10)
                bottomMargin = dp(16)
            },
        )

        var currentIndex = initialIndex

        fun renderCurrentItem() {
            if (
                generation != loadGeneration ||
                !GalleryMediaAccessPolicy.canRead(currentMediaAccessScope()) ||
                currentIndex !in items.indices
            ) {
                closeAuthorizedViewer()
                return
            }
            val item = items[currentIndex]
            val viewerCacheKey = thumbnailCacheKey(VIEWER_THUMBNAIL_NAMESPACE, item.contentUri)
            preview.setImageDrawable(null)
            preview.tag = viewerCacheKey
            preview.contentDescription = "Viewer for ${item.displayName}"
            viewerTitle.text = item.displayName
            viewerSubtitle.text = mediaMetadata(item)
            previous.isEnabled = currentIndex > 0
            previous.alpha = if (previous.isEnabled) 1f else 0.30f
            next.isEnabled = currentIndex < items.lastIndex
            next.alpha = if (next.isEnabled) 1f else 0.30f
            val isFavorite = item.contentUri in favoriteUris
            favorite.text = if (isFavorite) "♥ Saved" else "♡ Favorite"
            favorite.contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites"
            loadLocalThumbnail(item, preview, generation, VIEWER_THUMBNAIL_DP, VIEWER_THUMBNAIL_NAMESPACE)
        }

        previous.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex -= 1
                renderCurrentItem()
            }
        }
        next.setOnClickListener {
            if (currentIndex < items.lastIndex) {
                currentIndex += 1
                renderCurrentItem()
            }
        }
        share.setOnClickListener {
            val item = items.getOrNull(currentIndex) ?: return@setOnClickListener
            shareAuthorizedItem(item)
        }
        favorite.setOnClickListener {
            val item = items.getOrNull(currentIndex) ?: return@setOnClickListener
            toggleFavorite(item)
            renderCurrentItem()
        }
        more.setOnClickListener {
            val item = items.getOrNull(currentIndex) ?: return@setOnClickListener
            showItemDetails(item)
        }

        renderCurrentItem()
    }

    private fun closeAuthorizedViewer() {
        val overlay = viewerOverlay ?: return
        rootFrame.removeView(overlay)
        viewerOverlay = null
        applySystemChrome()
        renderCurrentDestination()
    }

    private fun shareAuthorizedItem(item: MediaItem) {
        val uri = Uri.parse(item.contentUri)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = item.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(Intent.createChooser(shareIntent, "Share with"))
        } catch (_: RuntimeException) {
            Toast.makeText(this, "No compatible share destination is available.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleFavorite(item: MediaItem) {
        if (item.contentUri in favoriteUris) {
            favoriteUris.remove(item.contentUri)
            Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show()
        } else {
            favoriteUris.add(item.contentUri)
            Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
        }
        persistFavorites()
        updateHeader()
    }

    private fun persistFavorites() {
        galleryPreferences()
            .edit()
            .putStringSet(FAVORITES_KEY, favoriteUris.toSet())
            .apply()
    }

    private fun showItemDetails(item: MediaItem) {
        val dimensions = if (item.width != null && item.height != null) "${item.width} × ${item.height}" else "Unknown"
        val duration = item.durationMillis?.let(::formatDuration) ?: "Not applicable"
        AlertDialog.Builder(this)
            .setTitle(item.displayName)
            .setMessage(
                listOf(
                    "Type: ${if (item.mimeType.startsWith("video/")) "Video" else "Photo"}",
                    "Album: ${item.albumName ?: "Not grouped"}",
                    "Date: ${DATE_TIME_FORMAT.format(item.capturedAt ?: item.modifiedAt)}",
                    "Dimensions: $dimensions",
                    "Duration: $duration",
                    "Size: ${formatBytes(item.sizeBytes)}",
                ).joinToString("\n"),
            )
            .setPositiveButton("Done", null)
            .show()
    }

    private fun renderSettings() {
        val settings = currentUserSettings()

        library.addView(settingsSectionHeader("Performance"))
        library.addView(
            settingChoiceRow(
                title = "File loading priority",
                subtitle = "Slow uses one thumbnail worker. Fast uses four local thumbnail workers.",
                value = settings.fileLoadingPriority.label,
            ) { showFileLoadingPriorityDialog() },
        )

        library.addView(settingsSectionHeader("Library"))
        library.addView(
            settingChoiceRow(
                title = "Manage included folders",
                subtitle = "Limit Gallery to selected folders from the current Android-authorized snapshot.",
                value = if (settings.includedAlbumIds.isEmpty()) "All" else settings.includedAlbumIds.size.toString(),
            ) { showFolderSelectionDialog(includeMode = true) },
        )
        library.addView(
            settingChoiceRow(
                title = "Manage excluded folders",
                subtitle = "Hide selected folders without changing Android media permission authority.",
                value = if (settings.excludedAlbumIds.isEmpty()) "None" else settings.excludedAlbumIds.size.toString(),
            ) { showFolderSelectionDialog(includeMode = false) },
        )
        library.addView(
            settingToggleRow(
                title = "Show hidden items",
                subtitle = "Shows hidden-looking items only when Android includes them in the authorized MediaStore snapshot.",
                checked = settings.showHiddenItems,
            ) { setBooleanSetting(SHOW_HIDDEN_ITEMS_KEY, it) },
        )

        library.addView(settingsSectionHeader("Playback"))
        library.addView(
            settingToggleRow(
                title = "Play videos automatically",
                subtitle = "Preference is saved now and will apply when native video playback is enabled.",
                checked = settings.playVideosAutomatically,
            ) { setBooleanSetting(PLAY_VIDEOS_AUTOMATICALLY_KEY, it) },
        )
        library.addView(
            settingToggleRow(
                title = "Loop videos",
                subtitle = "Preference is saved now and will apply when native video playback is enabled.",
                checked = settings.loopVideos,
            ) { setBooleanSetting(LOOP_VIDEOS_KEY, it) },
        )
        library.addView(
            settingToggleRow(
                title = "Animate GIFs in thumbnails",
                subtitle = "Preference is saved now; animated thumbnail decoding is not enabled in this Development build.",
                checked = settings.animateGifThumbnails,
            ) { setBooleanSetting(ANIMATE_GIF_THUMBNAILS_KEY, it) },
        )

        library.addView(settingsSectionHeader("Privacy & protection"))
        library.addView(
            settingChoiceRow(
                title = "Password protect photos",
                subtitle = "Requires the secure Protected Photos implementation and supported GoreeCloud/Android authentication.",
                value = "Not yet",
                enabled = true,
            ) { explainPasswordProtectionBoundary() },
        )

        library.addView(settingsSectionHeader("Deletion & recovery"))
        library.addView(
            settingToggleRow(
                title = "Delete empty folders after deleting their content",
                subtitle = "Preference is saved now; approved destructive media workflows are not enabled in this Development build.",
                checked = settings.deleteEmptyFolders,
            ) { setBooleanSetting(DELETE_EMPTY_FOLDERS_KEY, it) },
        )
        library.addView(
            settingToggleRow(
                title = "Move deleted items to Recycle Bin",
                subtitle = "Enabled by default. Preference applies when the approved Delete/Trash workflow is enabled.",
                checked = settings.moveDeletedItemsToRecycleBin,
            ) { setBooleanSetting(MOVE_DELETED_TO_RECYCLE_BIN_KEY, it) },
        )

        library.addView(settingsSectionHeader("Appearance"))
        library.addView(
            settingToggleRow(
                title = "Rounded-square thumbnails",
                subtitle = "Use GoreeCloud rounded-square clipping for media and album thumbnails.",
                checked = settings.roundedSquareThumbnails,
            ) { setBooleanSetting(ROUNDED_SQUARE_THUMBNAILS_KEY, it) },
        )

        library.addView(settingsSectionHeader("Cache"))
        library.addView(
            settingActionRow(
                title = "Clear cache",
                subtitle = "Clears the current in-memory thumbnail cache. Photos and videos are never deleted.",
                actionLabel = "Clear",
            ) {
                thumbnailCache.evictAll()
                Toast.makeText(this, "Thumbnail cache cleared", Toast.LENGTH_SHORT).show()
            },
        )

        library.addView(settingsSectionHeader("Favorites"))
        library.addView(
            settingActionRow(
                title = "Export Favorites",
                subtitle = "Export Gallery's local favorite content-URI list. Media files are not exported.",
                actionLabel = "Export",
            ) { createJsonDocument(EXPORT_FAVORITES_REQUEST, "GoreeCloud-Gallery-Favorites.json") },
        )
        library.addView(
            settingActionRow(
                title = "Import Favorites",
                subtitle = "Merge a Gallery Favorites export into the local Favorites set without expanding media permission.",
                actionLabel = "Import",
            ) { openJsonDocument(IMPORT_FAVORITES_REQUEST) },
        )

        library.addView(settingsSectionHeader("Settings portability"))
        library.addView(
            settingActionRow(
                title = "Export settings",
                subtitle = "Export non-secret Gallery preferences, including folder visibility selections.",
                actionLabel = "Export",
            ) { createJsonDocument(EXPORT_SETTINGS_REQUEST, "GoreeCloud-Gallery-Settings.json") },
        )
        library.addView(
            settingActionRow(
                title = "Import settings",
                subtitle = "Import a compatible GoreeCloud Gallery settings file. Unknown fields are ignored.",
                actionLabel = "Import",
            ) { openJsonDocument(IMPORT_SETTINGS_REQUEST) },
        )
    }

    private fun settingsSectionHeader(label: String): TextView = TextView(this).apply {
        text = label
        setTextColor(primaryTextColor())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f)
        setTypeface(typeface, Typeface.BOLD)
        setPadding(dp(2), dp(18), 0, dp(7))
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    private fun settingChoiceRow(
        title: String,
        subtitle: String,
        value: String,
        enabled: Boolean = true,
        onClick: () -> Unit,
    ): LinearLayout = settingBaseRow(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        trailing = settingsPill(value, emphasized = false),
        onClick = onClick,
    )

    private fun settingActionRow(
        title: String,
        subtitle: String,
        actionLabel: String,
        onClick: () -> Unit,
    ): LinearLayout = settingBaseRow(
        title = title,
        subtitle = subtitle,
        enabled = true,
        trailing = settingsPill(actionLabel, emphasized = true),
        onClick = onClick,
    )

    private fun settingToggleRow(
        title: String,
        subtitle: String,
        checked: Boolean,
        onToggle: (Boolean) -> Unit,
    ): LinearLayout = settingBaseRow(
        title = title,
        subtitle = subtitle,
        enabled = true,
        trailing = settingsPill(if (checked) "On" else "Off", emphasized = checked),
    ) {
        onToggle(!checked)
        renderSettingsDestinationOnly()
    }

    private fun settingBaseRow(
        title: String,
        subtitle: String,
        enabled: Boolean,
        trailing: View,
        onClick: () -> Unit,
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(68)
            setPadding(dp(14), dp(10), dp(10), dp(10))
            background = roundedSurface(
                withAlpha(primaryTextColor(), if (isNightMode()) 0.10f else 0.045f),
                17,
            )
            alpha = if (enabled) 1f else 0.55f

            val labels = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_VERTICAL
            }
            labels.addView(TextView(context).apply {
                text = title
                setTextColor(primaryTextColor())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f)
                setTypeface(typeface, Typeface.BOLD)
            })
            labels.addView(TextView(context).apply {
                text = subtitle
                setTextColor(secondaryTextColor())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f)
                setLineSpacing(0f, 1.06f)
                setPadding(0, dp(3), dp(8), 0)
            })
            addView(labels, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(trailing)

            isClickable = enabled
            isFocusable = enabled
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            contentDescription = "$title. $subtitle"
            if (enabled) setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                bottomMargin = dp(6)
            }
        }
    }

    private fun settingsPill(label: String, emphasized: Boolean): TextView = TextView(this).apply {
        text = label
        gravity = Gravity.CENTER
        minWidth = dp(52)
        minHeight = dp(36)
        setPadding(dp(10), 0, dp(10), 0)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f)
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(if (emphasized) accentColor() else primaryTextColor())
        background = roundedSurface(
            if (emphasized) withAlpha(accentColor(), 0.13f)
            else withAlpha(primaryTextColor(), if (isNightMode()) 0.10f else 0.055f),
            14,
        )
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    private fun renderSettingsDestinationOnly() {
        if (destination != GalleryDestination.SETTINGS) return
        updateHeader()
        library.removeAllViews()
        renderSettings()
    }

    private fun showFileLoadingPriorityDialog() {
        val current = currentUserSettings().fileLoadingPriority
        val values = GalleryFileLoadingPriority.entries.toTypedArray()
        val labels = values.map { it.label }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("File loading priority")
            .setSingleChoiceItems(labels, values.indexOf(current)) { dialog, which ->
                val selected = values[which]
                galleryPreferences().edit().putString(FILE_LOADING_PRIORITY_KEY, selected.storedValue).apply()
                reconfigureThumbnailExecutor(selected)
                renderSettingsDestinationOnly()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFolderSelectionDialog(includeMode: Boolean) {
        val albums = authorizedItems.buildAlbumCatalog().sortedBy { it.displayName.lowercase() }
        if (albums.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle(if (includeMode) "Included folders" else "Excluded folders")
                .setMessage(
                    "No authorized folders are available in the current Gallery snapshot. Grant media access from Photos first, or return after the library has loaded.",
                )
                .setPositiveButton("Done", null)
                .show()
            return
        }

        val key = if (includeMode) INCLUDED_ALBUM_IDS_KEY else EXCLUDED_ALBUM_IDS_KEY
        val selected = galleryPreferences().getStringSet(key, emptySet()).orEmpty().toMutableSet()
        val labels = albums.map { "${it.displayName} · ${itemCountLabel(it.itemCount)}" }.toTypedArray()
        val checked = BooleanArray(albums.size) { albums[it].id in selected }

        AlertDialog.Builder(this)
            .setTitle(if (includeMode) "Manage included folders" else "Manage excluded folders")
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                val albumId = albums[which].id
                if (isChecked) selected.add(albumId) else selected.remove(albumId)
            }
            .setPositiveButton("Save") { _, _ ->
                galleryPreferences().edit().putStringSet(key, selected.toSet()).apply()
                renderSettingsDestinationOnly()
            }
            .setNeutralButton("Clear") { _, _ ->
                galleryPreferences().edit().remove(key).apply()
                renderSettingsDestinationOnly()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun explainPasswordProtectionBoundary() {
        AlertDialog.Builder(this)
            .setTitle("Password protect photos")
            .setMessage(
                "Protected Photos is a required Gallery capability, but this Development build does not yet provide a secure protected-media store. " +
                    "Gallery will not fake protection with an app-local password toggle. The production implementation must use supported Android/GoreeCloud authentication, protected storage, Privacy Shield consent controls, and Wardveil trust boundaries before this setting becomes active.",
            )
            .setPositiveButton("Done", null)
            .show()
    }

    private fun setBooleanSetting(key: String, value: Boolean) {
        galleryPreferences().edit().putBoolean(key, value).apply()
    }

    private fun currentUserSettings(): GalleryUserSettings {
        val preferences = galleryPreferences()
        return GalleryUserSettings(
            fileLoadingPriority = GalleryFileLoadingPriority.fromStored(
                preferences.getString(FILE_LOADING_PRIORITY_KEY, GalleryFileLoadingPriority.FAST.storedValue),
            ),
            includedAlbumIds = preferences.getStringSet(INCLUDED_ALBUM_IDS_KEY, emptySet()).orEmpty().toSet(),
            excludedAlbumIds = preferences.getStringSet(EXCLUDED_ALBUM_IDS_KEY, emptySet()).orEmpty().toSet(),
            showHiddenItems = preferences.getBoolean(SHOW_HIDDEN_ITEMS_KEY, false),
            playVideosAutomatically = preferences.getBoolean(PLAY_VIDEOS_AUTOMATICALLY_KEY, false),
            loopVideos = preferences.getBoolean(LOOP_VIDEOS_KEY, false),
            animateGifThumbnails = preferences.getBoolean(ANIMATE_GIF_THUMBNAILS_KEY, false),
            deleteEmptyFolders = preferences.getBoolean(DELETE_EMPTY_FOLDERS_KEY, false),
            moveDeletedItemsToRecycleBin = preferences.getBoolean(MOVE_DELETED_TO_RECYCLE_BIN_KEY, true),
            roundedSquareThumbnails = preferences.getBoolean(ROUNDED_SQUARE_THUMBNAILS_KEY, true),
        )
    }

    private fun visibleAuthorizedItems(): List<MediaItem> =
        GallerySettingsPolicy.visibleItems(authorizedItems, currentUserSettings())

    private fun thumbnailCornerDp(defaultCornerDp: Int): Int =
        if (currentUserSettings().roundedSquareThumbnails) defaultCornerDp else 0

    private fun reconfigureThumbnailExecutor(priority: GalleryFileLoadingPriority) {
        val desiredWorkers = priority.thumbnailWorkerCount
        if (desiredWorkers == thumbnailWorkerCount) return
        thumbnailExecutor.shutdownNow()
        thumbnailWorkerCount = desiredWorkers
        thumbnailExecutor = Executors.newFixedThreadPool(thumbnailWorkerCount)
    }

    private fun createJsonDocument(requestCode: Int, suggestedName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, suggestedName)
        }
        try {
            startActivityForResult(intent, requestCode)
        } catch (_: RuntimeException) {
            Toast.makeText(this, "No document provider is available.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openJsonDocument(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        try {
            startActivityForResult(intent, requestCode)
        } catch (_: RuntimeException) {
            Toast.makeText(this, "No document provider is available.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun writeJsonDocument(uri: Uri, json: JSONObject, successMessage: String) {
        try {
            val stream = contentResolver.openOutputStream(uri) ?: throw IOException("Unable to open output document")
            stream.bufferedWriter(Charsets.UTF_8).use { it.write(json.toString(2)) }
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, "The export could not be written.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readJsonDocument(uri: Uri, consume: (JSONObject) -> Unit) {
        try {
            val stream = contentResolver.openInputStream(uri) ?: throw IOException("Unable to open input document")
            val text = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            consume(JSONObject(text))
        } catch (_: Exception) {
            Toast.makeText(this, "The selected Gallery file could not be imported.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildFavoritesExportJson(): JSONObject {
        val favorites = JSONArray()
        favoriteUris.sorted().forEach { favorites.put(it) }
        return JSONObject()
            .put("type", FAVORITES_EXPORT_TYPE)
            .put("schemaVersion", GallerySettingsPolicy.EXPORT_SCHEMA_VERSION)
            .put("favorites", favorites)
    }

    private fun importFavorites(json: JSONObject) {
        if (json.optString("type") != FAVORITES_EXPORT_TYPE) {
            throw IllegalArgumentException("Not a GoreeCloud Gallery Favorites export")
        }
        val array = json.getJSONArray("favorites")
        val before = favoriteUris.size
        for (index in 0 until array.length()) {
            val uri = array.optString(index).trim()
            if (uri.isNotBlank()) favoriteUris.add(uri)
        }
        persistFavorites()
        val added = favoriteUris.size - before
        Toast.makeText(
            this,
            if (added == 1) "Imported 1 new Favorite" else "Imported $added new Favorites",
            Toast.LENGTH_SHORT,
        ).show()
        updateHeader()
        renderSettingsDestinationOnly()
    }

    private fun buildSettingsExportJson(): JSONObject {
        val settings = currentUserSettings()
        return JSONObject()
            .put("type", SETTINGS_EXPORT_TYPE)
            .put("schemaVersion", GallerySettingsPolicy.EXPORT_SCHEMA_VERSION)
            .put("fileLoadingPriority", settings.fileLoadingPriority.storedValue)
            .put("includedAlbumIds", stringSetJson(settings.includedAlbumIds))
            .put("excludedAlbumIds", stringSetJson(settings.excludedAlbumIds))
            .put("showHiddenItems", settings.showHiddenItems)
            .put("playVideosAutomatically", settings.playVideosAutomatically)
            .put("loopVideos", settings.loopVideos)
            .put("animateGifThumbnails", settings.animateGifThumbnails)
            .put("deleteEmptyFolders", settings.deleteEmptyFolders)
            .put("moveDeletedItemsToRecycleBin", settings.moveDeletedItemsToRecycleBin)
            .put("roundedSquareThumbnails", settings.roundedSquareThumbnails)
    }

    private fun importSettings(json: JSONObject) {
        if (json.optString("type") != SETTINGS_EXPORT_TYPE) {
            throw IllegalArgumentException("Not a GoreeCloud Gallery settings export")
        }
        val current = currentUserSettings()
        val rawPriority = json.optString("fileLoadingPriority", current.fileLoadingPriority.storedValue)
        val importedPriority = GalleryFileLoadingPriority.entries.firstOrNull { it.storedValue == rawPriority }
            ?: throw IllegalArgumentException("Unsupported loading priority")

        galleryPreferences().edit()
            .putString(FILE_LOADING_PRIORITY_KEY, importedPriority.storedValue)
            .putStringSet(
                INCLUDED_ALBUM_IDS_KEY,
                json.optJSONArray("includedAlbumIds")?.let(::jsonStringSet) ?: current.includedAlbumIds,
            )
            .putStringSet(
                EXCLUDED_ALBUM_IDS_KEY,
                json.optJSONArray("excludedAlbumIds")?.let(::jsonStringSet) ?: current.excludedAlbumIds,
            )
            .putBoolean(SHOW_HIDDEN_ITEMS_KEY, json.optBoolean("showHiddenItems", current.showHiddenItems))
            .putBoolean(
                PLAY_VIDEOS_AUTOMATICALLY_KEY,
                json.optBoolean("playVideosAutomatically", current.playVideosAutomatically),
            )
            .putBoolean(LOOP_VIDEOS_KEY, json.optBoolean("loopVideos", current.loopVideos))
            .putBoolean(
                ANIMATE_GIF_THUMBNAILS_KEY,
                json.optBoolean("animateGifThumbnails", current.animateGifThumbnails),
            )
            .putBoolean(
                DELETE_EMPTY_FOLDERS_KEY,
                json.optBoolean("deleteEmptyFolders", current.deleteEmptyFolders),
            )
            .putBoolean(
                MOVE_DELETED_TO_RECYCLE_BIN_KEY,
                json.optBoolean("moveDeletedItemsToRecycleBin", current.moveDeletedItemsToRecycleBin),
            )
            .putBoolean(
                ROUNDED_SQUARE_THUMBNAILS_KEY,
                json.optBoolean("roundedSquareThumbnails", current.roundedSquareThumbnails),
            )
            .apply()

        reconfigureThumbnailExecutor(importedPriority)
        thumbnailCache.evictAll()
        Toast.makeText(this, "Gallery settings imported", Toast.LENGTH_SHORT).show()
        renderSettingsDestinationOnly()
    }

    private fun stringSetJson(values: Set<String>): JSONArray = JSONArray().apply {
        values.sorted().forEach { put(it) }
    }

    private fun jsonStringSet(array: JSONArray): Set<String> {
        val values = linkedSetOf<String>()
        for (index in 0 until array.length()) {
            val value = array.optString(index).trim()
            if (value.isNotBlank()) values.add(value)
        }
        return values
    }

    private fun galleryPreferences() = getSharedPreferences(LOCAL_STATE_PREFERENCES, MODE_PRIVATE)

    private fun viewerAction(
        label: String,
        enabled: Boolean,
        description: String,
        onClick: () -> Unit,
    ): TextView = TextView(this).apply {
        text = label
        gravity = Gravity.CENTER
        minHeight = dp(GalleryGlazeContract.GENERAL_TARGET_DP)
        minWidth = dp(GalleryGlazeContract.GENERAL_TARGET_DP)
        setTextColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTypeface(typeface, Typeface.BOLD)
        background = roundedSurface(0x26ffffff, 18)
        isEnabled = enabled
        isClickable = enabled
        isFocusable = enabled
        alpha = if (enabled) 1f else 0.35f
        contentDescription = description
        if (enabled) setOnClickListener { onClick() }
    }

    private fun iconHeaderAction(
        iconResource: Int,
        description: String,
        onClick: () -> Unit,
    ): ImageView = ImageView(this).apply {
        setImageResource(iconResource)
        setColorFilter(primaryTextColor())
        setPadding(dp(13), dp(13), dp(13), dp(13))
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        background = roundedSurface(withAlpha(primaryTextColor(), if (isNightMode()) 0.12f else 0.05f), 16)
        isClickable = true
        isFocusable = true
        contentDescription = description
        setOnClickListener { onClick() }
    }

    private fun toggleSearch() {
        if (destination == GalleryDestination.SETTINGS) return
        if (searchContainer.visibility == View.VISIBLE) {
            closeSearch()
        } else {
            searchContainer.visibility = View.VISIBLE
            searchField.requestFocus()
            searchControl.setImageResource(R.drawable.ic_gallery_close)
            searchControl.setColorFilter(primaryTextColor())
            searchControl.contentDescription = "Close search"
            searchContainer.post {
                (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)
                    ?.showSoftInput(searchField, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun closeSearch(clearQuery: Boolean = true) {
        if (!::searchContainer.isInitialized) return
        searchContainer.visibility = View.GONE
        searchControl.setImageResource(R.drawable.ic_gallery_search)
        searchControl.setColorFilter(primaryTextColor())
        searchControl.contentDescription = "Search the current Gallery destination"
        (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(searchField.windowToken, 0)
        if (clearQuery) {
            searchQuery = ""
            if (::searchField.isInitialized && searchField.text.isNotEmpty()) searchField.setText("")
            renderCurrentDestination()
        }
    }

    private fun matchesSearch(item: MediaItem): Boolean {
        if (searchQuery.isBlank()) return true
        val query = searchQuery.lowercase()
        return item.displayName.lowercase().contains(query) ||
            item.albumName?.lowercase()?.contains(query) == true
    }

    private fun sortOrderLabel(): String =
        if (selectedSort == MediaSortOrder.NEWEST) "Newest first" else "Oldest first"

    private fun sectionHeader(label: String): TextView = TextView(this).apply {
        text = label
        setTextColor(primaryTextColor())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        setTypeface(typeface, Typeface.BOLD)
        setPadding(dp(2), dp(16), 0, dp(7))
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    private fun dateGroupLabel(item: MediaItem): String {
        val date = (item.capturedAt ?: item.modifiedAt).atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()
        return when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> DATE_HEADER_FORMAT.format(date)
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

    private fun formatVideoBadge(item: MediaItem): String =
        item.durationMillis?.let(::formatDuration) ?: "VIDEO"

    private fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainder = seconds % 60
        return "$minutes:${remainder.toString().padStart(2, '0')}"
    }

    private fun itemCountLabel(count: Int): String =
        if (count == 1) "1 item" else "$count items"

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
        try {
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
        } catch (_: RuntimeException) {
            // Executor replacement can cancel queued thumbnail work; presentation remains safely empty until re-rendered.
        }
    }

    private fun thumbnailCacheKey(namespace: String, contentUri: String): String = "$namespace:$contentUri"

    private fun emptyState(title: String, message: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(28), dp(20), dp(20))
            addView(TextView(context).apply {
                text = title
                gravity = Gravity.CENTER
                setTextColor(primaryTextColor())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
                setTypeface(typeface, Typeface.BOLD)
            })
            addView(TextView(context).apply {
                text = message
                gravity = Gravity.CENTER
                setTextColor(secondaryTextColor())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
                setLineSpacing(0f, 1.08f)
                setPadding(0, dp(7), 0, 0)
            })
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(16)
            }
        }
    }

    private fun messageRow(title: String, message: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(24), dp(18), dp(24))
            background = roundedSurface(withAlpha(primaryTextColor(), if (isNightMode()) 0.10f else 0.045f), 20)
            addView(TextView(context).apply {
                text = title
                gravity = Gravity.CENTER
                setTextColor(primaryTextColor())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.5f)
                setTypeface(typeface, Typeface.BOLD)
            })
            addView(TextView(context).apply {
                text = message
                gravity = Gravity.CENTER
                setTextColor(secondaryTextColor())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
                setLineSpacing(0f, 1.08f)
                setPadding(0, dp(7), 0, 0)
            })
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(10)
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

    private fun applySystemChrome() {
        val canvas = canvasColor()
        window.statusBarColor = canvas
        window.navigationBarColor = canvas
        @Suppress("DEPRECATION")
        run {
            var flags = 0
            if (!isNightMode()) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            }
            window.decorView.systemUiVisibility = flags
        }
    }

    private fun isNightMode(): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    private fun canvasColor(): Int = themeColor(android.R.attr.colorBackground, 0xfffafafa.toInt())

    private fun primaryTextColor(): Int = themeColor(android.R.attr.textColorPrimary, 0xff1d1d1f.toInt())

    private fun secondaryTextColor(): Int = themeColor(android.R.attr.textColorSecondary, 0xff666666.toInt())

    private fun accentColor(): Int = themeColor(android.R.attr.colorAccent, 0xff2e7d6f.toInt())

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

    private data class AlbumPresentation(
        val id: String?,
        val name: String,
        val count: Int,
        val cover: MediaItem,
        val isFavorites: Boolean,
    )

    private enum class GalleryDestination {
        PHOTOS,
        ALBUMS,
        VIDEOS,
        SETTINGS,
    }

    private companion object {
        const val MEDIA_PERMISSION_REQUEST = 4101
        const val EXPORT_FAVORITES_REQUEST = 4201
        const val IMPORT_FAVORITES_REQUEST = 4202
        const val EXPORT_SETTINGS_REQUEST = 4203
        const val IMPORT_SETTINGS_REQUEST = 4204

        const val GRID_GAP_DP = 3
        const val GRID_CORNER_DP = 8
        const val GRID_THUMBNAIL_DP = 192
        const val ALBUM_GAP_DP = 12
        const val ALBUM_CORNER_DP = 16
        const val ALBUM_THUMBNAIL_DP = 320
        const val VIEWER_THUMBNAIL_DP = 720
        const val THUMBNAIL_CACHE_KIB = 8 * 1024
        const val GRID_THUMBNAIL_NAMESPACE = "grid"
        const val ALBUM_THUMBNAIL_NAMESPACE = "album"
        const val VIEWER_THUMBNAIL_NAMESPACE = "viewer"

        const val LOCAL_STATE_PREFERENCES = "goreecloud_gallery_local_state"
        const val FAVORITES_KEY = "favorite_content_uris"
        const val FILE_LOADING_PRIORITY_KEY = "file_loading_priority"
        const val INCLUDED_ALBUM_IDS_KEY = "included_album_ids"
        const val EXCLUDED_ALBUM_IDS_KEY = "excluded_album_ids"
        const val SHOW_HIDDEN_ITEMS_KEY = "show_hidden_items"
        const val PLAY_VIDEOS_AUTOMATICALLY_KEY = "play_videos_automatically"
        const val LOOP_VIDEOS_KEY = "loop_videos"
        const val ANIMATE_GIF_THUMBNAILS_KEY = "animate_gif_thumbnails"
        const val DELETE_EMPTY_FOLDERS_KEY = "delete_empty_folders"
        const val MOVE_DELETED_TO_RECYCLE_BIN_KEY = "move_deleted_items_to_recycle_bin"
        const val ROUNDED_SQUARE_THUMBNAILS_KEY = "rounded_square_thumbnails"

        const val FAVORITES_EXPORT_TYPE = "goreecloud-gallery-favorites"
        const val SETTINGS_EXPORT_TYPE = "goreecloud-gallery-settings"

        val DATE_TIME_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a").withZone(ZoneId.systemDefault())
        val DATE_HEADER_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

        fun formatBytes(bytes: Long): String = when {
            bytes >= 1024L * 1024L -> String.format("%.1f MiB", bytes / (1024.0 * 1024.0))
            bytes >= 1024L -> String.format("%.1f KiB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
