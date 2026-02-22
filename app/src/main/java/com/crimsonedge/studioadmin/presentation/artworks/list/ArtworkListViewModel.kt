package com.crimsonedge.studioadmin.presentation.artworks.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimsonedge.studioadmin.domain.model.Artwork
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

data class ArtworkListUiState(
    val artworks: Resource<List<Artwork>> = Resource.Loading,
    val types: List<ArtworkType> = emptyList(),
    val selectedType: String? = null,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val selectedIds: Set<Int> = emptySet(),
    val isSelectionMode: Boolean = false
)

@HiltViewModel
class ArtworkListViewModel @Inject constructor(
    private val artworkRepository: ArtworkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtworkListUiState())
    val uiState: StateFlow<ArtworkListUiState> = _uiState.asStateFlow()

    init {
        loadArtworks()
        loadTypes()
    }

    fun loadArtworks() {
        viewModelScope.launch {
            artworkRepository.getAll(_uiState.value.selectedType).collect { result ->
                _uiState.update { it.copy(artworks = result) }
            }
        }
    }

    fun loadTypes() {
        viewModelScope.launch {
            artworkRepository.getTypes().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(types = result.data) }
                    }
                    is Resource.Error -> { /* Types loading failed silently */ }
                    is Resource.Loading -> { /* Loading */ }
                }
            }
        }
    }

    fun setTypeFilter(type: String?) {
        _uiState.update { it.copy(selectedType = type) }
        loadArtworks()
    }

    fun deleteArtwork(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }
            artworkRepository.delete(id).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isDeleting = false) }
                        loadArtworks()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isDeleting = false, deleteError = result.message)
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isDeleting = true) }
                    }
                }
            }
        }
    }

    fun clearDeleteError() {
        _uiState.update { it.copy(deleteError = null) }
    }

    fun toggleSelection(id: Int) {
        _uiState.update { state ->
            val newIds = if (id in state.selectedIds) {
                state.selectedIds - id
            } else {
                state.selectedIds + id
            }
            state.copy(selectedIds = newIds, isSelectionMode = newIds.isNotEmpty())
        }
    }

    fun selectAll() {
        val artworks = (_uiState.value.artworks as? Resource.Success)?.data ?: return
        val allIds = artworks.map { it.id }.toSet()
        _uiState.update { it.copy(selectedIds = allIds, isSelectionMode = allIds.isNotEmpty()) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
    }

    fun deleteSelected() {
        val ids = _uiState.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            artworkRepository.deleteBatch(ids).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isDeleting = false, selectedIds = emptySet(), isSelectionMode = false) }
                        loadArtworks()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isDeleting = false, deleteError = result.message)
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isDeleting = true, deleteError = null) }
                    }
                }
            }
        }
    }
}
