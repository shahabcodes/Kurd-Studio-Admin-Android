package com.crimsonedge.studioadmin.presentation.siteconfig.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimsonedge.studioadmin.data.remote.dto.SectionRequest
import com.crimsonedge.studioadmin.domain.model.Section
import com.crimsonedge.studioadmin.domain.repository.SiteRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SectionEditState(
    val id: Int = 0,
    val tag: String = "",
    val title: String = "",
    val subtitle: String = "",
    val displayOrder: String = "0",
    val isActive: Boolean = true
)

data class SectionsListUiState(
    val sections: Resource<List<Section>> = Resource.Loading,
    val editingSectionId: Int? = null,
    val editState: SectionEditState = SectionEditState(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class SectionsListViewModel @Inject constructor(
    private val siteRepository: SiteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SectionsListUiState())
    val uiState: StateFlow<SectionsListUiState> = _uiState.asStateFlow()

    init {
        loadSections()
    }

    fun loadSections() {
        viewModelScope.launch {
            _uiState.update { it.copy(sections = Resource.Loading) }
            siteRepository.getSections().collect { result ->
                _uiState.update { it.copy(sections = result) }
            }
        }
    }

    fun startEdit(section: Section) {
        _uiState.update {
            it.copy(
                editingSectionId = section.id,
                editState = SectionEditState(
                    id = section.id,
                    tag = section.tag.orEmpty(),
                    title = section.title.orEmpty(),
                    subtitle = section.subtitle.orEmpty(),
                    displayOrder = section.displayOrder.toString(),
                    isActive = section.isActive
                ),
                error = null,
                saveSuccess = false
            )
        }
    }

    fun cancelEdit() {
        _uiState.update {
            it.copy(
                editingSectionId = null,
                editState = SectionEditState(),
                error = null
            )
        }
    }

    fun updateTag(value: String) {
        _uiState.update { it.copy(editState = it.editState.copy(tag = value)) }
    }

    fun updateTitle(value: String) {
        _uiState.update { it.copy(editState = it.editState.copy(title = value)) }
    }

    fun updateSubtitle(value: String) {
        _uiState.update { it.copy(editState = it.editState.copy(subtitle = value)) }
    }

    fun updateDisplayOrder(value: String) {
        _uiState.update { it.copy(editState = it.editState.copy(displayOrder = value)) }
    }

    fun updateIsActive(value: Boolean) {
        _uiState.update { it.copy(editState = it.editState.copy(isActive = value)) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun save() {
        val editState = _uiState.value.editState
        val sectionId = _uiState.value.editingSectionId ?: return

        val displayOrder = editState.displayOrder.trim().toIntOrNull() ?: 0

        val request = SectionRequest(
            tag = editState.tag.trim().ifBlank { null },
            title = editState.title.trim().ifBlank { null },
            subtitle = editState.subtitle.trim().ifBlank { null },
            displayOrder = displayOrder,
            isActive = editState.isActive
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            siteRepository.updateSection(sectionId, request).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                editingSectionId = null,
                                editState = SectionEditState(),
                                saveSuccess = true
                            )
                        }
                        loadSections()
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isSaving = false, error = result.message)
                        }
                    }
                }
            }
        }
    }
}
