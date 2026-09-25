package com.gorman.ourmemoryapp.data.firebase

object AccountKeys {
    private const val DOT = '.'
    private const val KEY_DOT = ','

    fun forEmail(email: String) = email.trim().lowercase().replace(DOT, KEY_DOT)
}
