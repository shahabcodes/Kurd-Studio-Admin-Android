package com.crimsonedge.studioadmin.presentation.siteconfig.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimsonedge.studioadmin.data.remote.dto.ProfileRequest
import com.crimsonedge.studioadmin.domain.repository.SiteRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileEditorUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val name: String = "",
    val tagline: String = "",
    val bio: String = "",
    val avatarImageId: Int? = null,
    val email: String = "",
    val instagramUrl: String = "",
    val twitterUrl: String = "",
    val artworksCount: String = "",
    val poemsCount: String = "",
    val yearsExperience: String = "",
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class ProfileEditorViewModel @Inject constructor(
    private val siteRepository: SiteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditorUiState())
    val uiState: StateFlow<ProfileEditorUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            siteRepository.getProfile().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val profile = result.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                name = profile.name,
                                tagline = profile.tagline.orEmpty(),
                                bio = profile.bio.orEmpty(),
                                avatarImageId = profile.avatarImageId,
                                email = profile.email.orEmpty(),
                                instagramUrl = profile.instagramUrl.orEmpty(),
                                twitterUrl = profile.twitterUrl.orEmpty(),
                                artworksCount = profile.artworksCount.orEmpty(),
                                poemsCount = profile.poemsCount.orEmpty(),
                                yearsExperience = profile.yearsExperience.orEmpty(),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, saveSuccess = false) }
    }

    fun updateTagline(value: String) {
        _uiState.update { it.copy(tagline = value, saveSuccess = false) }
    }

    fun updateBio(value: String) {
        _uiState.update { it.copy(bio = value, saveSuccess = false) }
    }

    fun updateAvatarImageId(value: Int?) {
        _uiState.update { it.copy(avatarImageId = value, saveSuccess = false) }
    }

    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value, saveSuccess = false) }
    }

    fun updateInstagramUrl(value: String) {
        _uiState.update { it.copy(instagramUrl = value, saveSuccess = false) }
    }

    fun updateTwitterUrl(value: String) {
        _uiState.update { it.copy(twitterUrl = value, saveSuccess = false) }
    }

    fun updateArtworksCount(value: String) {
        _uiState.update { it.copy(artworksCount = value, saveSuccess = false) }
    }

    fun updatePoemsCount(value: String) {
        _uiState.update { it.copy(poemsCount = value, saveSuccess = false) }
    }

    fun updateYearsExperience(value: String) {
        _uiState.update { it.copy(yearsExperience = value, saveSuccess = false) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }

        val request = ProfileRequest(
            name = state.name.trim(),
            tagline = state.tagline.trim().ifBlank { null },
            bio = state.bio.trim().ifBlank { null },
            avatarImageId = state.avatarImageId,
            email = state.email.trim().ifBlank { null },
            instagramUrl = state.instagramUrl.trim().ifBlank { null },
            twitterUrl = state.twitterUrl.trim().ifBlank { null },
            artworksCount = state.artworksCount.trim().ifBlank { null },
            poemsCount = state.poemsCount.trim().ifBlank { null },
            yearsExperience = state.yearsExperience.trim().ifBlank { null }
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            siteRepository.updateProfile(request).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isSaving = false, error = result.message) }
                    }
                }
            }
        }
    }
}
