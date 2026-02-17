package com.crimsonedge.studioadmin.presentation.artworks.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimsonedge.studioadmin.data.remote.dto.ArtworkRequest
import com.crimsonedge.studioadmin.domain.model.ArtworkType
import com.crimsonedge.studioadmin.domain.repository.ArtworkRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtworkFormUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val title: String = "",
    val slug: String = "",
    val artworkTypeId: Int = 0,
    val imageId: Int = 0,
    val description: String = "",
    val isFeatured: Boolean = false,
    val displayOrder: Int = 0,
    val types: List<ArtworkType> = emptyList(),
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val titleError: String? = null,
    val slugError: String? = null
)

@HiltViewModel
class ArtworkFormViewModel @Inject constructor(
    private val artworkRepository: ArtworkRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artworkId: Int? = savedStateHandle.get<String>("id")?.let {
        if (it != "new") it.toIntOrNull() else null
    }

    private val _uiState = MutableStateFlow(ArtworkFormUiState(isEditing = artworkId != null))
    val uiState: StateFlow<ArtworkFormUiState> = _uiState.asStateFlow()

    init {
        loadTypes()
        if (artworkId != null) {
            loadArtwork(artworkId)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadTypes() {
        viewModelScope.launch {
            artworkRepository.getTypes().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                types = result.data,
                                artworkTypeId = if (state.artworkTypeId == 0 && result.data.isNotEmpty())
                                    result.data.first().id else state.artworkTypeId
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadArtwork(id: Int) {
        viewModelScope.launch {
            artworkRepository.getById(id).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val artwork = result.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                title = artwork.title,
                                slug = artwork.slug,
                                artworkTypeId = artwork.artworkTypeId,
                                imageId = artwork.imageId,
                                description = artwork.description ?: "",
                                isFeatured = artwork.isFeatured,
                                displayOrder = artwork.displayOrder
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun updateTitle(value: String) {
        _uiState.update { it.copy(title = value, titleError = null) }
    }

    fun updateSlug(value: String) {
        _uiState.update { it.copy(slug = value, slugError = null) }
    }

    fun generateSlug() {
        val slug = _uiState.value.title
            .lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
        _uiState.update { it.copy(slug = slug, slugError = null) }
    }

    fun updateTypeId(id: Int) { _uiState.update { it.copy(artworkTypeId = id) } }
    fun updateImageId(id: Int) { _uiState.update { it.copy(imageId = id) } }
    fun updateDescription(value: String) { _uiState.update { it.copy(description = value) } }
    fun updateFeatured(value: Boolean) { _uiState.update { it.copy(isFeatured = value) } }
    fun updateDisplayOrder(value: String) {
        val order = value.toIntOrNull() ?: 0
        _uiState.update { it.copy(displayOrder = order) }
    }

    fun save() {
        val state = _uiState.value
        var hasError = false

        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            hasError = true
        }
        if (state.slug.isBlank()) {
            _uiState.update { it.copy(slugError = "Slug is required") }
            hasError = true
        }
        if (hasError) return

        val request = ArtworkRequest(
            title = state.title,
            slug = state.slug,
            artworkTypeId = state.artworkTypeId,
            imageId = state.imageId,
            description = state.description.ifBlank { null },
            isFeatured = state.isFeatured,
            displayOrder = state.displayOrder
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val flow = if (artworkId != null) {
                artworkRepository.update(artworkId, request)
            } else {
                artworkRepository.create(request)
            }
            flow.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isSaving = false, error = result.message) }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                }
            }
        }
    }
}
