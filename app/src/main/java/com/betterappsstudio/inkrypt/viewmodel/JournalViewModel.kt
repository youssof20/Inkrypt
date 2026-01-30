package com.betterappsstudio.inkrypt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.betterappsstudio.inkrypt.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JournalViewModel(
    private val repository: JournalRepository
) : ViewModel() {
    
    private val _entries = MutableStateFlow<List<JournalRepository.DecryptedEntry>>(emptyList())
    val entries: StateFlow<List<JournalRepository.DecryptedEntry>> = _entries.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<JournalRepository.DecryptedEntry>>(emptyList())
    val searchResults: StateFlow<List<JournalRepository.DecryptedEntry>> = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    init {
        loadEntries()
    }
    
    private fun loadEntries() {
        viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
                _entries.value = entries
            }
        }
    }
    
    fun search(query: String) {
        if (query.isBlank()) {
            _isSearching.value = false
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            _isSearching.value = true
            val results = repository.searchEntries(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }
    
    suspend fun getEntry(id: Long): JournalRepository.DecryptedEntry? {
        return repository.getEntryById(id)
    }
    
    fun saveEntry(entry: JournalRepository.DecryptedEntry) {
        viewModelScope.launch {
            if (entry.id == 0L) {
                repository.insertEntry(entry)
            } else {
                repository.updateEntry(entry.copy(updatedAt = System.currentTimeMillis()))
            }
            loadEntries()
        }
    }
    
    fun deleteEntry(entry: JournalRepository.DecryptedEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            loadEntries()
        }
    }
    
    companion object {
        fun provideFactory(repository: JournalRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return JournalViewModel(repository) as T
                }
            }
        }
    }
}

