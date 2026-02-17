package com.crimsonedge.studioadmin.presentation.siteconfig.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimsonedge.studioadmin.data.remote.dto.SiteSettingRequest
import com.crimsonedge.studioadmin.domain.model.SiteSetting
import com.crimsonedge.studioadmin.domain.repository.SiteRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsListUiState(
    val settings: Resource<List<SiteSetting>> = Resource.Loading,
    val editingKey: String? = null,
    val editingValue: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class SettingsListViewModel @Inject constructor(
    private val siteRepository: SiteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsListUiState())
    val uiState: StateFlow<SettingsListUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(settings = Resource.Loading) }
            siteRepository.getSettings().collect { result ->
                _uiState.update { it.copy(settings = result) }
            }
        }
    }

    fun startEdit(setting: SiteSetting) {
        _uiState.update {
            it.copy(
                editingKey = setting.settingKey,
                editingValue = setting.settingValue.orEmpty(),
                error = null,
                saveSuccess = false
            )
        }
    }

    fun updateEditValue(value: String) {
        _uiState.update { it.copy(editingValue = value) }
    }

    fun cancelEdit() {
        _uiState.update {
            it.copy(
                editingKey = null,
                editingValue = "",
                error = null
            )
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun saveEdit() {
        val key = _uiState.value.editingKey ?: return
        val value = _uiState.value.editingValue.trim().ifBlank { null }
        val request = SiteSettingRequest(settingValue = value)

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            siteRepository.updateSetting(key, request).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                editingKey = null,
                                editingValue = "",
                                saveSuccess = true
                            )
                        }
                        loadSettings()
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
