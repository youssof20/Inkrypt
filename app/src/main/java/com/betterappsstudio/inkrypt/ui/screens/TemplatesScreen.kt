package com.betterappsstudio.inkrypt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.betterappsstudio.inkrypt.repository.JournalRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    templates: List<JournalRepository.DecryptedTemplate>,
    onTemplateClick: (JournalRepository.DecryptedTemplate) -> Unit,
    onNewTemplate: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNewTemplate) {
                        Icon(Icons.Default.Add, contentDescription = "New Template")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewTemplate) {
                Icon(Icons.Default.Add, contentDescription = "New Template")
            }
        }
    ) { padding ->
        if (templates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "No templates yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates) { template ->
                    TemplateItem(
                        template = template,
                        onClick = { onTemplateClick(template) }
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateItem(
    template: JournalRepository.DecryptedTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

