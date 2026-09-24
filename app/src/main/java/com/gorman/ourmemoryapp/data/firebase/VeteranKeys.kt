package com.gorman.ourmemoryapp.data.firebase

object VeteranKeys {
    private const val PREFIX = "veteran"

    fun forId(veteranId: String) = "$PREFIX$veteranId"
}
