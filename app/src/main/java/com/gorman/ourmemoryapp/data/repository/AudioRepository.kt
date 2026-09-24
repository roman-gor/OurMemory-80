package com.gorman.ourmemoryapp.data.repository

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.gorman.ourmemoryapp.domain.models.AudioItem
import com.gorman.ourmemoryapp.domain.models.AudioPlaybackState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AudioRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState = _playbackState.asStateFlow()

    fun playAudio(audioItem: AudioItem) {
        stopAudio()
        runCatching {
            mediaPlayer = MediaPlayer.create(context, audioItem.rawResourceId).apply {
                setOnCompletionListener {
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        currentPosition = 0,
                        currentAudio = null
                    )
                }
                start()
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = true,
                    currentAudio = audioItem,
                    duration = duration
                )
            }
        }.onFailure { error ->
            Log.e("Audio Repository", "Error play audio", error)
        }
    }

    fun pauseAudio() {
        mediaPlayer?.pause()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            currentPosition = mediaPlayer?.currentPosition ?: 0
        )
    }

    fun resumeAudio() {
        mediaPlayer?.start()
        _playbackState.value = _playbackState.value.copy(isPlaying = true)
    }

    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _playbackState.value = AudioPlaybackState()
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        _playbackState.value = _playbackState.value.copy(currentPosition = position)
    }

    fun observePosition(): Flow<Int> = flow {
        while (true) {
            emit(mediaPlayer?.currentPosition ?: 0)
            delay(POSITION_UPDATE_MILLIS)
        }
    }

    companion object {
        private const val POSITION_UPDATE_MILLIS = 500L
    }
}
