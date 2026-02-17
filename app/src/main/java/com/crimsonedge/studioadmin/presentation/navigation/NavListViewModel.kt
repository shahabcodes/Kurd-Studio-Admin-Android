package com.crimsonedge.studioadmin.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimsonedge.studioadmin.data.remote.dto.NavigationItemRequest
import com.crimsonedge.studioadmin.domain.model.NavigationItem
import com.crimsonedge.studioadmin.domain.repository.NavigationRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NavListUiState(
    val navItems: Resource<List<NavigationItem>> = Resource.Loading,
    val showAddDialog: Boolean = false,
    val editingItem: NavigationItem? = null,
    val deletingItem: NavigationItem? = null,
    val isDeleting: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NavListViewModel @Inject constructor(
    private val navigationRepository: NavigationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NavListUiState())
    val uiState: StateFlow<NavListUiState> = _uiState.asStateFlow()

    init {
        loadNavItems()
    }

    fun loadNavItems() {
        viewModelScope.launch {
            navigationRepository.getNavItems().collect { result ->
                _uiState.update { it.copy(navItems = result) }
            }
        }
    }

    fun addNavItem(request: NavigationItemRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            navigationRepository.createNavItem(request).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(isSaving = false, showAddDialog = false)
                        }
                        loadNavItems()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isSaving = false, error = result.message)
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                }
            }
        }
    }

    fun updateNavItem(id: Int, request: NavigationItemRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            navigationRepository.updateNavItem(id, request).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(isSaving = false, editingItem = null)
                        }
                        loadNavItems()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isSaving = false, error = result.message)
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                }
            }
        }
    }

    fun deleteNavItem(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            navigationRepository.deleteNavItem(id).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(isDeleting = false, deletingItem = null)
                        }
                        loadNavItems()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isDeleting = false, error = result.message)
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isDeleting = true) }
                    }
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun dismissAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun showEditDialog(item: NavigationItem) {
        _uiState.update { it.copy(editingItem = item) }
    }

    fun dismissEditDialog() {
        _uiState.update { it.copy(editingItem = null) }
    }

    fun showDeleteConfirmation(item: NavigationItem) {
        _uiState.update { it.copy(deletingItem = item) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(deletingItem = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
