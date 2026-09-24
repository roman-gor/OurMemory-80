package com.gorman.ourmemoryapp.data.firebase

import com.google.firebase.database.DataSnapshot

inline fun <reified T> DataSnapshot.childrenAs(): List<T> =
    children.mapNotNull { child -> runCatching { child.getValue(T::class.java) }.getOrNull() }
