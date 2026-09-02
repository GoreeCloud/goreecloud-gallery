package com.goreecloud.gallery

/** Pure presentation policy for Gallery-owned transient notices. */
object GalleryTransientNoticePolicy {
    const val MaxMessageCodePoints = 180
    const val DisplayDurationMs = 2400L

    fun normalize(message: String): String {
        val collapsed = message.trim().replace(Regex("\\s+"), " ")
        if (collapsed.isEmpty()) return ""
        if (collapsed.codePointCount(0, collapsed.length) <= MaxMessageCodePoints) return collapsed

        val visibleCodePoints = MaxMessageCodePoints - 1
        val end = collapsed.offsetByCodePoints(0, visibleCodePoints)
        return collapsed.substring(0, end).trimEnd() + "…"
    }
}
