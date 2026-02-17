package com.crimsonedge.studioadmin.presentation.writings.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimsonedge.studioadmin.data.remote.dto.WritingRequest
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

data class WritingFormUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val title: String = "",
    val slug: String = "",
    val writingTypeId: Int = 0,
    val subtitle: String = "",
    val excerpt: String = "",
    val fullContent: String = "",
    val datePublished: String = "",
    val novelName: String = "",
    val chapterNumber: String = "",
    val displayOrder: String = "0",
    val types: List<WritingType> = emptyList(),
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val titleError: String? = null,
    val slugError: String? = null
)

@HiltViewModel
class WritingFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val writingRepository: WritingRepository
) : ViewModel() {

    private val writingId: Int? = savedStateHandle.get<String>("id")?.let {
        if (it == "new") null else it.toIntOrNull()
    }

    private val _uiState = MutableStateFlow(WritingFormUiState(isEditing = writingId != null))
    val uiState: StateFlow<WritingFormUiState> = _uiState.asStateFlow()

    init {
        loadTypes()
        if (writingId != null) {
            loadWriting(writingId)
        }
    }

    private fun loadTypes() {
        viewModelScope.launch {
            writingRepository.getTypes().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { state ->
                            val typeId = if (state.writingTypeId == 0 && result.data.isNotEmpty()) {
                                result.data.first().id
                            } else {
                                state.writingTypeId
                            }
                            state.copy(types = result.data, writingTypeId = typeId)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                    is Resource.Loading -> { /* Loading */ }
                }
            }
        }
    }

    private fun loadWriting(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            writingRepository.getById(id).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val writing = result.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                title = writing.title,
                                slug = writing.slug,
                                writingTypeId = writing.writingTypeId,
                                subtitle = writing.subtitle ?: "",
                                excerpt = writing.excerpt ?: "",
                                fullContent = writing.fullContent ?: "",
                                datePublished = writing.datePublished ?: "",
                                novelName = writing.novelName ?: "",
                                chapterNumber = writing.chapterNumber?.toString() ?: "",
                                displayOrder = writing.displayOrder.toString()
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
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

    fun updateWritingTypeId(value: Int) {
        _uiState.update { it.copy(writingTypeId = value) }
    }

    fun updateSubtitle(value: String) {
        _uiState.update { it.copy(subtitle = value) }
    }

    fun updateExcerpt(value: String) {
        _uiState.update { it.copy(excerpt = value) }
    }

    fun updateFullContent(value: String) {
        _uiState.update { it.copy(fullContent = value) }
    }

    fun updateDatePublished(value: String) {
        _uiState.update { it.copy(datePublished = value) }
    }

    fun updateNovelName(value: String) {
        _uiState.update { it.copy(novelName = value) }
    }

    fun updateChapterNumber(value: String) {
        _uiState.update { it.copy(chapterNumber = value) }
    }

    fun updateDisplayOrder(value: String) {
        _uiState.update { it.copy(displayOrder = value) }
    }

    fun generateSlug() {
        val slug = _uiState.value.title
            .lowercase()
            .trim()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .replace(Regex("-+"), "-")
            .trimEnd('-')
        _uiState.update { it.copy(slug = slug, slugError = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun save() {
        val state = _uiState.value

        // Validation
        var hasError = false
        var titleError: String? = null
        var slugError: String? = null

        if (state.title.isBlank()) {
            titleError = "Title is required"
            hasError = true
        }
        if (state.slug.isBlank()) {
            slugError = "Slug is required"
            hasError = true
        }

        if (hasError) {
            _uiState.update { it.copy(titleError = titleError, slugError = slugError) }
            return
        }

        val request = WritingRequest(
            title = state.title.trim(),
            slug = state.slug.trim(),
            writingTypeId = state.writingTypeId,
            subtitle = state.subtitle.ifBlank { null },
            excerpt = state.excerpt.ifBlank { null },
            fullContent = state.fullContent.ifBlank { null },
            datePublished = state.datePublished.ifBlank { null },
            novelName = state.novelName.ifBlank { null },
            chapterNumber = state.chapterNumber.toIntOrNull(),
            displayOrder = state.displayOrder.toIntOrNull() ?: 0
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val flow = if (writingId != null) {
                writingRepository.update(writingId, request)
            } else {
                writingRepository.create(request)
            }

            flow.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
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
}
