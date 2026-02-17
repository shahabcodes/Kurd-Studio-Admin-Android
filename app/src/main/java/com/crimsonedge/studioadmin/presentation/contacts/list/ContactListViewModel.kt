package com.crimsonedge.studioadmin.presentation.contacts.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimsonedge.studioadmin.domain.model.Contact
import com.crimsonedge.studioadmin.domain.repository.ContactRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactListUiState(
    val contacts: Resource<List<Contact>> = Resource.Loading,
    val unreadOnly: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteError: String? = null
)

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactListUiState())
    val uiState: StateFlow<ContactListUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            val unreadOnly = if (_uiState.value.unreadOnly) true else null
            contactRepository.getAll(unreadOnly).collect { result ->
                _uiState.update { it.copy(contacts = result) }
            }
        }
    }

    fun setUnreadFilter(unreadOnly: Boolean) {
        _uiState.update { it.copy(unreadOnly = unreadOnly) }
        loadContacts()
    }

    fun deleteContact(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }
            contactRepository.delete(id).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isDeleting = false) }
                        loadContacts()
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
