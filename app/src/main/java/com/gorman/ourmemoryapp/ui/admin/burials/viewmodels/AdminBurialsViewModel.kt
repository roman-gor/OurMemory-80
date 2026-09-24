package com.gorman.ourmemoryapp.ui.admin.burials.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.admin.burials.models.AdminBurialsUiState
import com.gorman.ourmemoryapp.ui.admin.burials.models.toAdminItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AdminBurialsViewModel @Inject constructor(
    private val burialsRepository: BurialsRepository,
    private val veteransRepository: VeteransRepository
) : ViewModel() {

    private val reloads = MutableStateFlow(0)

    val uiState = observeAdminBurialsUiState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AdminBurialsUiState.Loading
    )

    fun onScreenResumed() {
        reloads.update { it + 1 }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAdminBurialsUiState() = reloads.flatMapLatest {
        flow {
            val veterans = runCatching { veteransRepository.getAllVeterans() }.getOrDefault(emptyList())
            val namesByBurial = veterans.filter { it.burialId.isNotBlank() }
                .groupBy({ it.burialId }, { it.name })
            val items = burialsRepository.getAllBurials()
                .sortedWith(
                    compareBy({ it.section.toIntOrNull() }, { it.row.toIntOrNull() }, { it.place.toIntOrNull() })
                )
                .map { it.toAdminItemUi(veteranNames = namesByBurial[it.id].orEmpty().joinToString(NAMES_SEPARATOR)) }
            emit(AdminBurialsUiState.Success(items.toPersistentList()))
        }
    }.catch<AdminBurialsUiState> { error ->
        Log.e(LOG_TAG, "Failed to load burials", error)
        emit(AdminBurialsUiState.Error)
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "AdminBurialsViewModel"
        private const val NAMES_SEPARATOR = ", "
    }
}
