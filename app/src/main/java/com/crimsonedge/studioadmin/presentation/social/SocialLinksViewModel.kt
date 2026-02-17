package com.crimsonedge.studioadmin.presentation.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimsonedge.studioadmin.data.remote.dto.SocialLinkRequest
import com.crimsonedge.studioadmin.domain.model.SocialLink
import com.crimsonedge.studioadmin.domain.repository.NavigationRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialLinksUiState(
    val socialLinks: Resource<List<SocialLink>> = Resource.Loading,
    val showAddDialog: Boolean = false,
    val editingLink: SocialLink? = null,
    val deletingLink: SocialLink? = null,
    val isDeleting: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SocialLinksViewModel @Inject constructor(
    private val navigationRepository: NavigationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialLinksUiState())
    val uiState: StateFlow<SocialLinksUiState> = _uiState.asStateFlow()

    init {
        loadSocialLinks()
    }

    fun loadSocialLinks() {
        viewModelScope.launch {
            navigationRepository.getSocialLinks().collect { result ->
                _uiState.update { it.copy(socialLinks = result) }
            }
        }
    }

    fun addSocialLink(request: SocialLinkRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            navigationRepository.createSocialLink(request).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(isSaving = false, showAddDialog = false)
                        }
                        loadSocialLinks()
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

    fun updateSocialLink(id: Int, request: SocialLinkRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            navigationRepository.updateSocialLink(id, request).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(isSaving = false, editingLink = null)
                        }
                        loadSocialLinks()
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

    fun deleteSocialLink(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            navigationRepository.deleteSocialLink(id).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(isDeleting = false, deletingLink = null)
                        }
                        loadSocialLinks()
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

    fun showEditDialog(link: SocialLink) {
        _uiState.update { it.copy(editingLink = link) }
    }

    fun dismissEditDialog() {
        _uiState.update { it.copy(editingLink = null) }
    }

    fun showDeleteConfirmation(link: SocialLink) {
        _uiState.update { it.copy(deletingLink = link) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(deletingLink = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
