package com.crimsonedge.studioadmin.presentation.images

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimsonedge.studioadmin.domain.model.ImageMeta
import com.crimsonedge.studioadmin.domain.repository.ImageRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

data class ImageListUiState(
    val images: Resource<List<ImageMeta>> = Resource.Loading,
    val isUploading: Boolean = false,
    val isDeleting: Boolean = false,
    val selectedImage: ImageMeta? = null,
    val error: String? = null
)

@HiltViewModel
class ImageListViewModel @Inject constructor(
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageListUiState())
    val uiState: StateFlow<ImageListUiState> = _uiState.asStateFlow()

    init { loadImages() }

    fun loadImages() {
        viewModelScope.launch {
            imageRepository.getAll().collect { result ->
                _uiState.update { it.copy(images = result) }
            }
        }
    }

    fun uploadImage(part: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            imageRepository.upload(part).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isUploading = false) }
                        loadImages() // Refresh list after successful upload
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isUploading = false, error = result.message) }
                    }
                    Resource.Loading -> {
                        _uiState.update { it.copy(isUploading = true) }
                    }
                }
            }
        }
    }

    fun selectImage(image: ImageMeta?) {
        _uiState.update { it.copy(selectedImage = image) }
    }

    fun deleteImage(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            imageRepository.delete(id).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isDeleting = false, selectedImage = null) }
                        loadImages()
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isDeleting = false, error = result.message) }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
