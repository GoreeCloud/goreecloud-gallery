package com.goreecloud.gallery.core

fun <T> mediaGridRows(items: List<T>, columns: Int = 2): List<List<T>> {
    require(columns > 0) { "grid columns must be positive" }
    return items.chunked(columns)
}
