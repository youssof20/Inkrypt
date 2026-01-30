package com.betterappsstudio.inkrypt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.betterappsstudio.inkrypt.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TemplateViewModel(
    private val repository: JournalRepository
) : ViewModel() {
    
    private val _templates = MutableStateFlow<List<JournalRepository.DecryptedTemplate>>(emptyList())
    val templates: StateFlow<List<JournalRepository.DecryptedTemplate>> = _templates.asStateFlow()
    
    init {
        loadTemplates()
    }
    
    private fun loadTemplates() {
        viewModelScope.launch {
            repository.getAllTemplates().collect { templates ->
                _templates.value = templates
            }
        }
    }
    
    suspend fun getTemplate(id: Long): JournalRepository.DecryptedTemplate? {
        return repository.getTemplateById(id)
    }
    
    fun saveTemplate(template: JournalRepository.DecryptedTemplate) {
        viewModelScope.launch {
            if (template.id == 0L) {
                repository.insertTemplate(template)
            } else {
                repository.updateTemplate(template)
            }
            loadTemplates()
        }
    }
    
    fun deleteTemplate(template: JournalRepository.DecryptedTemplate) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
            loadTemplates()
        }
    }
    
    companion object {
        fun provideFactory(repository: JournalRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TemplateViewModel(repository) as T
                }
            }
        }
    }
}

