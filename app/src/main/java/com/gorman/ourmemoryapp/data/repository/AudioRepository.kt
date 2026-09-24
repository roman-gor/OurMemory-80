package com.gorman.ourmemoryapp.data.repository

import android.content.Context
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
    @ApplicationContext context: Context
) {
    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState = _playbackState.asStateFlow()

    private val player = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (_playbackState.value.currentAudio != null) {
                    _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> if (duration != C.TIME_UNSET) {
                        _playbackState.value = _playbackState.value.copy(duration = duration.toInt())
                    }
                    Player.STATE_ENDED -> stopAudio()
                    else -> Unit
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(LOG_TAG, "Error playing audio", error)
                stopAudio()
            }
        })
    }

    fun playAudio(audioItem: AudioItem) {
        player.setMediaItem(MediaItem.fromUri(audioItem.url))
        player.prepare()
        player.play()
        _playbackState.value = AudioPlaybackState(isPlaying = true, currentAudio = audioItem)
    }

    fun pauseAudio() {
        player.pause()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            currentPosition = player.currentPosition.toInt()
        )
    }

    fun resumeAudio() {
        player.play()
    }

    fun stopAudio() {
        player.stop()
        player.clearMediaItems()
        _playbackState.value = AudioPlaybackState()
    }

    fun seekTo(position: Int) {
        player.seekTo(position.toLong())
        _playbackState.value = _playbackState.value.copy(currentPosition = position)
    }

    fun observePosition(): Flow<Int> = flow {
        while (true) {
            emit(player.currentPosition.toInt())
            delay(POSITION_UPDATE_MILLIS)
        }
    }

    fun release() {
        player.release()
    }

    companion object {
        private const val LOG_TAG = "AudioRepository"
        private const val POSITION_UPDATE_MILLIS = 500L
    }
}
