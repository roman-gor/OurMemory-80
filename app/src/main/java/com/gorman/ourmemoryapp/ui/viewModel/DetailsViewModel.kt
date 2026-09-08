package com.gorman.ourmemoryapp.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.data.repository.AudioRepository
import com.gorman.ourmemoryapp.domain.models.AudioItem
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.domain.models.VeteranUiState
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel(assistedFactory = DetailsViewModel.Factory::class)
class DetailsViewModel @AssistedInject constructor(
    @Assisted private val veteranId: String,
    private val _repository: VeteransRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(veteranId: String): DetailsViewModel
    }

    val uiState: StateFlow<DetailsUiState> = flow {
        val veterans = _repository.getAllVeterans()
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

        emit(
            DetailsUiState.Success(
                veteran = filteredVeteran,
                rewards = rewardsList,
                additionalInfo = infoList.toPersistentList(),
                additionalRes = initialMap.toPersistentMap(),
                directUrls = directUrls.toPersistentMap(),
                additionalText = textList.toPersistentList()
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

    private suspend fun loadDirectedUrlSequentially(urls: Map<String, String>): Map<String, String> {
        val loadedUrls = mutableMapOf<String, String>()
        urls.forEach { (key, value) ->
            if (key.contains("yandex")) {
                runCatching {
                    val response = _repository.getHrefFromLink(publicKey = key)
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
}


    fun loadAudioForVeteran(veteranId: String) {
        val audioItems = when (veteranId) {
            "10" -> listOf(
                AudioItem(
                    id = 10,
                    title = "Биография ветерана",
                    fileName = "veteran_bio_10.mp3",
                    rawResourceId = R.raw.veteran_bio_10,
                    itemId = 10
                ),

            )
            "17" -> listOf(
                AudioItem(
                    id = 17,
                    title = "Биография ветерана",
                    fileName = "veteran_bio_17.mp3",
                    rawResourceId = com.gorman.ourmemoryapp.R.raw.veteran_bio_17,
                    itemId = 17
                ),

                )
            else -> emptyList()
        }

        _audioList.value = audioItems
    }
    fun playFirstAudioForVeteran(veteranId: String) {
        val audioItem = when (veteranId) {
            "10" -> AudioItem(
                id = 10,
                title = "Биография ветерана",
                fileName = "veteran_bio_10.mp3",
                rawResourceId = com.gorman.ourmemoryapp.R.raw.veteran_bio_10,
                itemId = 1
            )
            "17" -> AudioItem(
                id = 17,
                title = "Биография ветерана",
                fileName = "veteran_bio_17.mp3",
                rawResourceId = com.gorman.ourmemoryapp.R.raw.veteran_bio_17,
                itemId = 2
            )
            else -> null
        }

        audioItem?.let {
            playAudio(it)
        }
    }
    fun playAudio(audioItem: AudioItem) {
        viewModelScope.launch {
            try {
                audioRepository.stopAudio()
                audioRepository.playAudio(audioItem)
            } catch (e: Exception) {
                Log.e("DetailsViewModel", "Error playing audio", e)
            }
        }
    }

    fun pauseAudio() {
        audioRepository.pauseAudio()
    }

    fun resumeAudio() {
        audioRepository.resumeAudio()
    }

    fun stopAudio() {
        audioRepository.stopAudio()
    }

    fun seekTo(position: Int) {
        audioRepository.seekTo(position)
    }

    fun getPlaybackState(): Flow<AudioRepository.PlaybackState> {
        return audioRepository.playbackState
    }

    companion object {
        fun loadAudioForVeteran() {
            TODO("Not yet implemented")
        }
    }
}
