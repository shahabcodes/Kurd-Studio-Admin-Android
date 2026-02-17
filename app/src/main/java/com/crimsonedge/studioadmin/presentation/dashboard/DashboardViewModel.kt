package com.crimsonedge.studioadmin.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimsonedge.studioadmin.data.local.TokenDataStore
import com.crimsonedge.studioadmin.domain.model.DashboardStats
import com.crimsonedge.studioadmin.domain.repository.DashboardRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<Resource<DashboardStats>>(Resource.Loading)
    val dashboardState: StateFlow<Resource<DashboardStats>> = _dashboardState.asStateFlow()

    val displayName: StateFlow<String?> = tokenDataStore.displayName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            repository.getStats().collect { result ->
                _dashboardState.value = result
            }
        }
    }
}
