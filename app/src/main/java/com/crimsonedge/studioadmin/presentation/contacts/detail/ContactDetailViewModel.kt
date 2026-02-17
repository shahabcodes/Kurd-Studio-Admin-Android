package com.crimsonedge.studioadmin.presentation.contacts.detail

import androidx.lifecycle.SavedStateHandle
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

data class ContactDetailUiState(
    val contact: Resource<Contact> = Resource.Loading,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: Int = savedStateHandle.get<Int>("id") ?: 0

    private val _uiState = MutableStateFlow(ContactDetailUiState())
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    init {
        loadContact()
    }

    private fun loadContact() {
        viewModelScope.launch {
            contactRepository.getAll(null).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val contact = result.data.find { it.id == contactId }
                        if (contact != null) {
                            _uiState.update { it.copy(contact = Resource.Success(contact)) }
                            // Auto-mark as read
                            if (!contact.isRead) {
                                markAsRead()
                            }
                        } else {
                            _uiState.update {
                                it.copy(contact = Resource.Error("Contact not found"))
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(contact = Resource.Error(result.message)) }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(contact = Resource.Loading) }
                    }
                }
            }
        }
    }

    private fun markAsRead() {
        viewModelScope.launch {
            contactRepository.markAsRead(contactId).collect { /* fire and forget */ }
        }
    }

    fun deleteContact() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            contactRepository.delete(contactId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isDeleting = false, error = result.message) }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isDeleting = true) }
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
