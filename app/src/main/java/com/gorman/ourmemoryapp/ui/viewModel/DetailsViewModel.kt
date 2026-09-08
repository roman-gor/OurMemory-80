package com.gorman.ourmemoryapp.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.data.repository.AudioRepository
import com.gorman.ourmemoryapp.domain.models.AudioItem
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.states.AudioAction
import com.gorman.ourmemoryapp.ui.states.DetailsUiEvent
import com.gorman.ourmemoryapp.ui.states.DetailsUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailsViewModel.Factory::class)
class DetailsViewModel @AssistedInject constructor(
    @Assisted private val veteranId: String,
    private val veteranRepository: VeteransRepository,
    private val audioRepository: AudioRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(veteranId: String): DetailsViewModel
    }

    val playbackState = audioRepository.playbackState

    val uiState: StateFlow<DetailsUiState> = flow {
        val veterans = veteranRepository.getAllVeterans()
        val filteredVeteran = veterans.find { it.id == veteranId }
            ?: error("Veteran with ID = $veteranId was not found")

        val rewards = filteredVeteran.rewards
        val rewardsList = if (rewards.isEmpty()) {
            persistentListOf()
        } else {
            rewards
                .split(',')
                .mapNotNull { it.toIntOrNull() }
                .toPersistentList()
        }

        val infoList = filteredVeteran.veteransInfo
        val initialMap = mutableMapOf<String, String>()
        val textList = mutableListOf<String>()

        infoList.forEach { info ->
            if (info.contains("http")) {
                if (info.contains("|")) {
                    val url = info.split("|").getOrNull(0) ?: ""
                    val describe = info.split("|").getOrNull(1) ?: ""
                    initialMap[url] = describe
                } else {
                    initialMap[info] = ""
                }
            } else {
                textList.add(info)
            }
        }

        val directUrls = loadDirectedUrlSequentially(initialMap)
        val audioVeteran = loadAudioForVeteran(veteranId)

        emit(
            DetailsUiState.Success(
                veteran = filteredVeteran,
                rewards = rewardsList,
                additionalInfo = infoList.toPersistentList(),
                additionalRes = initialMap.toPersistentMap(),
                directUrls = directUrls.toPersistentMap(),
                additionalText = textList.toPersistentList(),
                audio = audioVeteran
            ) as DetailsUiState
        )
    }.flowOn(
        Dispatchers.IO
    ).catch { error ->
        emit(DetailsUiState.Error(error))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DetailsUiState.Loading
    )

    fun onUiEvent(event: DetailsUiEvent) {
        when (event) {
            is DetailsUiEvent.OnAudioAction -> {
                when (event.action) {
                    AudioAction.Play -> playAudioForVeteran()
                    AudioAction.Pause -> pauseAudio()
                    AudioAction.Resume -> resumeAudio()
                    AudioAction.Stop -> stopAudio()
                    is AudioAction.SeekTo -> seekTo(event.action.position)
                }
            }
        }
    }

    private suspend fun loadDirectedUrlSequentially(urls: Map<String, String>): Map<String, String> {
        val loadedUrls = mutableMapOf<String, String>()
        urls.forEach { (key, value) ->
            if (key.contains("yandex")) {
                runCatching {
                    val response = veteranRepository.getHrefFromLink(publicKey = key)
                    response.href?.let { href ->
                        loadedUrls[href] = value
                    }
                }.onFailure {
                    loadedUrls[key] = value
                }
            } else {
                loadedUrls[key] = value
            }
        }
        return loadedUrls
    }

    private fun loadAudioForVeteran(veteranId: String): AudioItem {
        return when (veteranId) {
            else -> AudioItem(
                id = 10,
                title = "Биография ветерана",
                fileName = "veteran_bio_10.mp3",
                rawResourceId = R.raw.veteran_bio_10,
                itemId = 10
            )
        }
    }
    private fun playAudioForVeteran() {
        (uiState.value as? DetailsUiState.Success)?.audio?.let {
            playAudio(it)
        }
    }
    private fun playAudio(audioItem: AudioItem) {
        viewModelScope.launch {
            runCatching {
                audioRepository.stopAudio()
                audioRepository.playAudio(audioItem)
            }.onFailure { error ->
                Log.e("DetailsViewModel", "Error playing audio", error)
            }
        }
    }

    private fun pauseAudio() {
        audioRepository.pauseAudio()
    }

    private fun resumeAudio() {
        audioRepository.resumeAudio()
    }

    private fun stopAudio() {
        audioRepository.stopAudio()
    }

    private fun seekTo(position: Int) {
        audioRepository.seekTo(position)
    }
}
