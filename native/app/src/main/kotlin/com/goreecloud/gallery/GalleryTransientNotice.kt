package com.goreecloud.gallery

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

/**
 * Gallery-owned, non-modal transient feedback surface.
 *
 * This is a bounded native mapping of the Glaze UI 2.2 GlzToast standard variant for
 * application-owned confirmation/status feedback. It keeps short-lived feedback inside the
 * first-party Gallery surface rather than delegating presentation to platform Toast UI.
 * Presentation never changes MediaStore, Favorites, settings, import/export, or sharing authority.
 */
class GalleryTransientNoticeHost(
    private val root: FrameLayout,
    private val surfaceColor: () -> Int,
    private val textColor: () -> Int,
    private val accentColor: () -> Int,
) {
    private var activeNotice: TextView? = null
    private val dismissRunnable = Runnable { dismiss() }

    fun show(message: String) {
        val normalized = GalleryTransientNoticePolicy.normalize(message)
        if (normalized.isEmpty()) return

        dismiss()
        val density = root.resources.displayMetrics.density
        val notice = TextView(root.context).apply {
            text = normalized
            setTextColor(textColor())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, GalleryGlazeContract.TRANSIENT_NOTICE_TEXT_SP)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            minHeight = (GalleryGlazeContract.TRANSIENT_NOTICE_STANDARD_HEIGHT_DP * density).toInt()
            setPadding(
                (GalleryGlazeContract.TRANSIENT_NOTICE_HORIZONTAL_PADDING_DP * density).toInt(),
                (10f * density).toInt(),
                (GalleryGlazeContract.TRANSIENT_NOTICE_HORIZONTAL_PADDING_DP * density).toInt(),
                (10f * density).toInt(),
            )
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = GalleryGlazeContract.TRANSIENT_NOTICE_RADIUS_DP * density
                setColor(surfaceColor())
                setStroke(maxOf(1, density.toInt()), withAlpha(accentColor(), 0.24f))
            }
            elevation = 8f * density
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        activeNotice = notice
        root.addView(
            notice,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                marginStart = (16f * density).toInt()
                marginEnd = (16f * density).toInt()
                bottomMargin = (GalleryGlazeContract.NAVIGATION_RESERVED_SPACE_DP * density).toInt()
            },
        )
        notice.announceForAccessibility(normalized)
        notice.postDelayed(dismissRunnable, GalleryTransientNoticePolicy.DisplayDurationMs)
    }

    fun dismiss() {
        val notice = activeNotice ?: return
        notice.removeCallbacks(dismissRunnable)
        if (notice.parent === root) root.removeView(notice)
        activeNotice = null
    }

    private fun withAlpha(color: Int, alpha: Float): Int {
        val clamped = alpha.coerceIn(0f, 1f)
        return Color.argb(
            (255 * clamped).toInt(),
            Color.red(color),
            Color.green(color),
            Color.blue(color),
        )
    }
}
