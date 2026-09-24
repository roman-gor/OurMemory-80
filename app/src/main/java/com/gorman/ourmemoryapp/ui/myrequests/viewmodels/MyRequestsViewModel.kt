package com.gorman.ourmemoryapp.ui.myrequests.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.repository.MyRequestsRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.common.models.observeVeteranNames
import com.gorman.ourmemoryapp.ui.myrequests.models.MyRequestsUiState
import com.gorman.ourmemoryapp.ui.myrequests.models.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRequestsViewModel @Inject constructor(
    private val myRequestsRepository: MyRequestsRepository,
    veteransRepository: VeteransRepository
) : ViewModel() {

    val uiState = combine(
        myRequestsRepository.observeMyRequests(),
        veteransRepository.observeVeteranNames()
    ) { requests, names ->
        MyRequestsUiState.Success(
            requests.map { it.toUi(veteranName = names[it.veteranId].orEmpty()) }.toPersistentList()
        )
    }.catch<MyRequestsUiState> { error ->
        Log.e(LOG_TAG, "Failed to load my requests", error)
        emit(MyRequestsUiState.Error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = MyRequestsUiState.Loading
    )

    fun onScreenResumed() {
        viewModelScope.launch { myRequestsRepository.markAllSeen() }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "MyRequestsViewModel"
    }
}
