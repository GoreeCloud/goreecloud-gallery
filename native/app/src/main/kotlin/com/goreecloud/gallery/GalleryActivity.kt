package com.goreecloud.gallery

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class GalleryActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var action: Button
    private lateinit var library: LinearLayout
    private val thumbnailExecutor = Executors.newFixedThreadPool(THUMBNAIL_WORKERS)
    private var loadGeneration = 0

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = flags or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            }
            window.decorView.systemUiVisibility = flags
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(gutter, dp(24), gutter, dp(40))
            setBackgroundColor(canvas)
        }

        content.addView(TextView(this).apply {
            setText("Local library")
            setTextColor(secondaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            isAllCaps = false
            importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
        })
        content.addView(TextView(this).apply {
            setText("Gallery")
            setTextColor(primaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 34f)
            setPadding(0, dp(4), 0, 0)
        })
        content.addView(TextView(this).apply {
            setText("First-party Android shell · Glaze UI ${GalleryGlazeContract.VERSION} source target")
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
        content.addView(action, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(12)
        })

        content.addView(TextView(this).apply {
            setText("Recent media")
            setTextColor(primaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setPadding(0, dp(26), 0, dp(4))
        })
        content.addView(TextView(this).apply {
            setText("Newest authorized MediaStore rows and local thumbnails are shown without network access or cloud dependency.")
            setTextColor(secondaryTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(0, 0, 0, dp(8))
        })

        library = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(library, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        })
    }

    private fun renderPermissionState() {
        val accessScope = currentMediaAccessScope()
        if (!GalleryMediaAccessPolicy.canRead(accessScope)) {
            loadGeneration += 1
            status.text = "Media permission is required before Gallery can read the local library."
            action.isEnabled = true
            action.text = "Choose media access"
            action.setOnClickListener { requestReadableMediaAccess() }
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
                val result = AndroidMediaStoreReader(contentResolver)
                    .readLatest(GalleryGlazeContract.MAX_RENDERED_MEDIA_ROWS)
                runOnUiThread {
                    if (generation != loadGeneration) return@runOnUiThread
                    action.isEnabled = true
                    status.text = buildString {
                        append(accessScopeLabel(accessScope))
                        append(" · Authorized local library: ${result.items.size} item")
                        if (result.items.size != 1) append('s')
                        if (result.rejectedRowCount > 0) append(" · ${result.rejectedRowCount} malformed row(s) skipped")
                    }
                    renderItems(result.items, generation)
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
            action.isEnabled = true
            status.text = message
            library.removeAllViews()
            library.addView(messageRow("No media list is shown because the authoritative provider read did not succeed."))
        }
    }

    private fun renderItems(items: List<MediaItem>, generation: Int) {
        library.removeAllViews()
        if (items.isEmpty()) {
            library.addView(messageRow("No authorized image or video rows were returned."))
            return
        }
        items.forEach { library.addView(mediaRow(it, generation)) }
    }

    private fun mediaRow(item: MediaItem, generation: Int): LinearLayout {
        val primaryTextColor = themeColor(android.R.attr.textColorPrimary, 0xff1d1d1f.toInt())
        val secondaryTextColor = themeColor(android.R.attr.textColorSecondary, 0xff666666.toInt())
        val surface = themeColor(android.R.attr.colorBackgroundFloating, themeColor(android.R.attr.colorBackground, 0xfffafafa.toInt()))
        val thumbnail = ImageView(this).apply {
            tag = item.contentUri
            contentDescription = "Thumbnail for ${item.displayName}"
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = roundedSurface(themeColor(android.R.attr.colorControlHighlight, 0x14000000), 14)
        }
        loadLocalThumbnail(item, thumbnail, generation)

        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(context).apply {
                setText(item.displayName)
                setTextColor(primaryTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            })
            addView(TextView(context).apply {
                val timestamp = item.capturedAt ?: item.modifiedAt
                val kind = if (item.mimeType.startsWith("video/")) "Video" else "Image"
                setText(listOfNotNull(
                    kind,
                    item.albumName?.let { "Album: $it" },
                    DATE_TIME_FORMAT.format(timestamp),
                    formatBytes(item.sizeBytes),
                ).joinToString(" · "))
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
            addView(thumbnail, LinearLayout.LayoutParams(dp(THUMBNAIL_DP), dp(THUMBNAIL_DP)).apply {
                marginEnd = dp(12)
            })
            addView(details, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(8)
            }
        }
    }

    private fun loadLocalThumbnail(item: MediaItem, target: ImageView, generation: Int) {
        thumbnailExecutor.execute {
            val bitmap = try {
                contentResolver.loadThumbnail(
                    Uri.parse(item.contentUri),
                    Size(dp(THUMBNAIL_DP), dp(THUMBNAIL_DP)),
                    null,
                )
            } catch (_: SecurityException) {
                null
            } catch (_: RuntimeException) {
                null
            }
            if (bitmap == null) return@execute
            runOnUiThread {
                if (generation == loadGeneration && target.tag == item.contentUri) {
                    target.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun messageRow(message: String): TextView = TextView(this).apply {
        setText(message)
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
                readMediaVisualUserSelected = Build.VERSION.SDK_INT >= 34 &&
                    granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED),
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
            Build.VERSION.SDK_INT >= 33 -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
            )
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
        const val THUMBNAIL_WORKERS = 2
        val DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter
            .ofPattern("MMM d, yyyy · h:mm a")
            .withZone(ZoneId.systemDefault())

        fun formatBytes(bytes: Long): String = when {
            bytes >= 1024L * 1024L -> String.format("%.1f MiB", bytes / (1024.0 * 1024.0))
            bytes >= 1024L -> String.format("%.1f KiB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
