package com.gorman.ourmemoryapp.ui.admin.common.models

fun <T> List<T>.moved(index: Int, offset: Int): List<T> {
    val target = index + offset
    if (index !in indices || target !in indices) return this
    return toMutableList().apply { add(target, removeAt(index)) }
}
