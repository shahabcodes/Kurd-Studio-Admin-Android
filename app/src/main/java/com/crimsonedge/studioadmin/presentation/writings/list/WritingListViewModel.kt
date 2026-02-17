package com.crimsonedge.studioadmin.presentation.writings.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimsonedge.studioadmin.domain.model.Writing
import com.crimsonedge.studioadmin.domain.model.WritingType
import com.crimsonedge.studioadmin.domain.repository.WritingRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WritingListUiState(
    val writings: Resource<List<Writing>> = Resource.Loading,
    val types: List<WritingType> = emptyList(),
    val selectedType: String? = null,
    val isDeleting: Boolean = false,
    val deleteError: String? = null
)

@HiltViewModel
class WritingListViewModel @Inject constructor(
    private val writingRepository: WritingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WritingListUiState())
    val uiState: StateFlow<WritingListUiState> = _uiState.asStateFlow()

    init {
        loadWritings()
        loadTypes()
    }

    fun loadWritings() {
        viewModelScope.launch {
            writingRepository.getAll(_uiState.value.selectedType).collect { result ->
                _uiState.update { it.copy(writings = result) }
            }
        }
    }

    fun loadTypes() {
        viewModelScope.launch {
            writingRepository.getTypes().collect { result ->
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
        loadWritings()
    }

    fun deleteWriting(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }
            writingRepository.delete(id).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isDeleting = false) }
                        loadWritings()
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
}
