package com.crimsonedge.studioadmin.presentation.siteconfig.hero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimsonedge.studioadmin.data.remote.dto.HeroRequest
import com.crimsonedge.studioadmin.domain.repository.SiteRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HeroEditorUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val quote: String = "",
    val quoteAttribution: String = "",
    val headline: String = "",
    val subheading: String = "",
    val featuredImageId: Int? = null,
    val badgeText: String = "",
    val primaryCtaText: String = "",
    val primaryCtaLink: String = "",
    val secondaryCtaText: String = "",
    val secondaryCtaLink: String = "",
    val isActive: Boolean = true,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class HeroEditorViewModel @Inject constructor(
    private val siteRepository: SiteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HeroEditorUiState())
    val uiState: StateFlow<HeroEditorUiState> = _uiState.asStateFlow()

    init {
        loadHero()
    }

    fun loadHero() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            siteRepository.getHero().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val hero = result.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                quote = hero.quote.orEmpty(),
                                quoteAttribution = hero.quoteAttribution.orEmpty(),
                                headline = hero.headline.orEmpty(),
                                subheading = hero.subheading.orEmpty(),
                                featuredImageId = hero.featuredImageId,
                                badgeText = hero.badgeText.orEmpty(),
                                primaryCtaText = hero.primaryCtaText.orEmpty(),
                                primaryCtaLink = hero.primaryCtaLink.orEmpty(),
                                secondaryCtaText = hero.secondaryCtaText.orEmpty(),
                                secondaryCtaLink = hero.secondaryCtaLink.orEmpty(),
                                isActive = hero.isActive,
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

    fun updateQuote(value: String) {
        _uiState.update { it.copy(quote = value, saveSuccess = false) }
    }

    fun updateQuoteAttribution(value: String) {
        _uiState.update { it.copy(quoteAttribution = value, saveSuccess = false) }
    }

    fun updateHeadline(value: String) {
        _uiState.update { it.copy(headline = value, saveSuccess = false) }
    }

    fun updateSubheading(value: String) {
        _uiState.update { it.copy(subheading = value, saveSuccess = false) }
    }

    fun updateFeaturedImageId(value: Int?) {
        _uiState.update { it.copy(featuredImageId = value, saveSuccess = false) }
    }

    fun updateBadgeText(value: String) {
        _uiState.update { it.copy(badgeText = value, saveSuccess = false) }
    }

    fun updatePrimaryCtaText(value: String) {
        _uiState.update { it.copy(primaryCtaText = value, saveSuccess = false) }
    }

    fun updatePrimaryCtaLink(value: String) {
        _uiState.update { it.copy(primaryCtaLink = value, saveSuccess = false) }
    }

    fun updateSecondaryCtaText(value: String) {
        _uiState.update { it.copy(secondaryCtaText = value, saveSuccess = false) }
    }

    fun updateSecondaryCtaLink(value: String) {
        _uiState.update { it.copy(secondaryCtaLink = value, saveSuccess = false) }
    }

    fun updateIsActive(value: Boolean) {
        _uiState.update { it.copy(isActive = value, saveSuccess = false) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun save() {
        val state = _uiState.value

        val request = HeroRequest(
            quote = state.quote.trim().ifBlank { null },
            quoteAttribution = state.quoteAttribution.trim().ifBlank { null },
            headline = state.headline.trim().ifBlank { null },
            subheading = state.subheading.trim().ifBlank { null },
            featuredImageId = state.featuredImageId,
            badgeText = state.badgeText.trim().ifBlank { null },
            primaryCtaText = state.primaryCtaText.trim().ifBlank { null },
            primaryCtaLink = state.primaryCtaLink.trim().ifBlank { null },
            secondaryCtaText = state.secondaryCtaText.trim().ifBlank { null },
            secondaryCtaLink = state.secondaryCtaLink.trim().ifBlank { null },
            isActive = state.isActive
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            siteRepository.updateHero(request).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                        loadHero()
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isSaving = false, error = result.message) }
                    }
                }
            }
        }
    }
}
