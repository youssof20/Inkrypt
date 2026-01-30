package com.betterappsstudio.inkrypt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.betterappsstudio.inkrypt.repository.JournalRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditorScreen(
    entry: JournalRepository.DecryptedEntry?,
    onSave: (JournalRepository.DecryptedEntry) -> Unit,
    onDelete: (JournalRepository.DecryptedEntry) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(entry?.title ?: "") }
    var content by remember { mutableStateOf(entry?.content ?: "") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entry == null) "New Entry" else "Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (entry != null) {
                        IconButton(
                            onClick = {
                                onDelete(entry)
                                onBack()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(
                        onClick = {
                            val newEntry = JournalRepository.DecryptedEntry(
                                id = entry?.id ?: 0L,
                                title = title,
                                content = content,
                                createdAt = entry?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                templateId = entry?.templateId
                            )
                            onSave(newEntry)
                            onBack()
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp),
                minLines = 15
            )
        }
    }
}

