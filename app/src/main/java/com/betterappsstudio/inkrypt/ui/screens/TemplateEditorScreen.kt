package com.betterappsstudio.inkrypt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.betterappsstudio.inkrypt.repository.JournalRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditorScreen(
    template: JournalRepository.DecryptedTemplate?,
    onSave: (JournalRepository.DecryptedTemplate) -> Unit,
    onBack: () -> Unit
) {
    var name by remember(template) { mutableStateOf(template?.name ?: "") }
    var content by remember(template) { mutableStateOf(template?.content ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (template == null) "New template" else "Edit template") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            when {
                                name.isBlank() -> error = "Name is required"
                                else -> {
                                    error = null
                                    onSave(
                                        JournalRepository.DecryptedTemplate(
                                            id = template?.id ?: 0L,
                                            name = name.trim(),
                                            content = content.trim()
                                        )
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            error?.let { msg ->
                Text(
                    msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; error = null },
                label = { Text("Template name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                maxLines = 20
            )
        }
    }
}
