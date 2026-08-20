package com.crowdshield.stampede.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crowdshield.stampede.domain.VideoAnalysisResult
import com.crowdshield.stampede.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffCommandCenterViewModel @Inject constructor(
    private val repository: IncidentRepository
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    fun uploadVideo(uri: Uri) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Uploading
            repository.uploadVideo(uri).fold(
                onSuccess = { result ->
                    _uploadState.value = UploadState.Success(result)
                },
                onFailure = { error ->
                    _uploadState.value = UploadState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }
}

sealed class UploadState {
    object Idle : UploadState()
    object Uploading : UploadState()
    data class Success(val result: VideoAnalysisResult) : UploadState()
    data class Error(val message: String) : UploadState()
}
