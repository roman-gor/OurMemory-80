package com.gorman.ourmemoryapp.domain.models

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val currentAudio: AudioItem? = null,
    val currentPosition: Int = 0,
    val duration: Int = 0
)
