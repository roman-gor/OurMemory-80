package com.gorman.ourmemoryapp.data.repository

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.gorman.ourmemoryapp.domain.models.AudioItem
import com.gorman.ourmemoryapp.domain.models.AudioPlaybackState
import com.gorman.ourmemoryapp.playback.PlaybackService
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

    private var controller: MediaController? = null
    private val pendingCommands = mutableListOf<(MediaController) -> Unit>()

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            val currentAudio = _playbackState.value.currentAudio ?: return
            val isOurItem = player.currentMediaItem?.mediaId == currentAudio.id
            if (!isOurItem || player.playbackState == Player.STATE_ENDED) {
                if (isOurItem || player.currentMediaItem != null) _playbackState.value = AudioPlaybackState()
                return
            }
            _playbackState.value = _playbackState.value.copy(
                isPlaying = player.isPlaying,
                duration = player.duration.takeIf { it != C.TIME_UNSET }?.toInt() ?: _playbackState.value.duration
            )
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(LOG_TAG, "Error playing audio", error)
            _playbackState.value = AudioPlaybackState()
        }
    }

    private val controllerFuture = MediaController.Builder(
        context,
        SessionToken(context, ComponentName(context, PlaybackService::class.java))
    ).buildAsync().also { future ->
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess(::onControllerConnected)
                    .onFailure { Log.e(LOG_TAG, "Failed to connect to playback service", it) }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun playAudio(audioItem: AudioItem) {
        _playbackState.value = AudioPlaybackState(isPlaying = true, currentAudio = audioItem)
        withController {
            it.setMediaItem(audioItem.toMediaItem())
            it.prepare()
            it.play()
        }
    }

    fun pauseAudio() {
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            currentPosition = controller?.currentPosition?.toInt() ?: _playbackState.value.currentPosition
        )
        withController { it.pause() }
    }

    fun resumeAudio() {
        withController { it.play() }
    }

    fun stopAudio() {
        withController {
            it.stop()
            it.clearMediaItems()
        }
        _playbackState.value = AudioPlaybackState()
    }

    fun seekTo(position: Int) {
        _playbackState.value = _playbackState.value.copy(currentPosition = position)
        withController { it.seekTo(position.toLong()) }
    }

    fun observePosition(): Flow<Int> = flow {
        while (true) {
            emit(controller?.currentPosition?.toInt() ?: 0)
            delay(POSITION_UPDATE_MILLIS)
        }
    }

    fun release() {
        val ownsPlayback = controller?.currentMediaItem?.mediaId == _playbackState.value.currentAudio?.id
        if (ownsPlayback) stopAudio()
        controller?.removeListener(playerListener)
        pendingCommands.clear()
        MediaController.releaseFuture(controllerFuture)
    }

    private fun onControllerConnected(connected: MediaController) {
        controller = connected
        connected.addListener(playerListener)
        pendingCommands.forEach { it(connected) }
        pendingCommands.clear()
    }

    private fun withController(command: (MediaController) -> Unit) {
        controller?.let(command) ?: pendingCommands.add(command)
    }

    private fun AudioItem.toMediaItem() = MediaItem.Builder()
        .setMediaId(id)
        .setUri(url)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(subtitle)
                .build()
        )
        .build()

    companion object {
        private const val LOG_TAG = "AudioRepository"
        private const val POSITION_UPDATE_MILLIS = 500L
    }
}
