package com.betterappsstudio.inkrypt.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.betterappsstudio.inkrypt.data.MediaManager
import com.betterappsstudio.inkrypt.repository.JournalRepository
import com.betterappsstudio.inkrypt.ui.components.ImageGallery
import com.betterappsstudio.inkrypt.ui.components.TagChips
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EnhancedEntryEditorScreen(
    entry: JournalRepository.DecryptedEntry?,
    mediaManager: MediaManager,
    onSave: (JournalRepository.DecryptedEntry) -> Unit,
    onDelete: (JournalRepository.DecryptedEntry) -> Unit,
    onBack: () -> Unit
) {
    var title by rememberSaveable(stateSaver = androidx.compose.runtime.saveable.autoSaver()) { mutableStateOf(entry?.title ?: "") }
    var content by rememberSaveable(stateSaver = androidx.compose.runtime.saveable.autoSaver()) { mutableStateOf(entry?.content ?: "") }
    val stableEntryId = entry?.id ?: 0L
    var tags by remember(stableEntryId) { mutableStateOf(entry?.tags ?: emptyList()) }
    var imagePaths by remember(stableEntryId) { mutableStateOf(entry?.imagePaths ?: emptyList()) }
    var showTagInput by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
    )
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val imagePath = mediaManager.saveImage(it)
                    imagePaths = imagePaths + imagePath
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entry == null) "New Entry" else "Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
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
                                templateId = entry?.templateId,
                                tags = tags,
                                imagePaths = imagePaths,
                                voiceNotePath = entry?.voiceNotePath
                            )
                            onSave(newEntry)
                            onBack()
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
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title - Notion-style large title
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Untitled", style = MaterialTheme.typography.displaySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.displaySmall,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tags - minimal, unobtrusive
            if (tags.isNotEmpty() || showTagInput) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (tags.isNotEmpty()) {
                        TagChips(tags = tags) { tagToRemove ->
                            tags = tags.filter { it != tagToRemove }
                        }
                    }
                    
                    if (showTagInput) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newTag,
                                onValueChange = { newTag = it },
                                placeholder = { Text("Tag name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(
                                onClick = {
                                    if (newTag.isNotBlank() && !tags.contains(newTag.trim())) {
                                        tags = tags + newTag.trim()
                                        newTag = ""
                                        showTagInput = false
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Add")
                            }
                            IconButton(onClick = { showTagInput = false }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Cancel", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            
            // Add tag button - only show when not in input mode
            if (!showTagInput) {
                TextButton(
                    onClick = { showTagInput = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add tag", style = MaterialTheme.typography.labelMedium)
                }
            }
            
            if (tags.isNotEmpty() || imagePaths.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
            
            // Images
            if (imagePaths.isNotEmpty()) {
                ImageGallery(
                    imagePaths = imagePaths,
                    mediaManager = mediaManager,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Media buttons - minimal, unobtrusive
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (permissionsState.allPermissionsGranted) {
                            imagePicker.launch("image/*")
                        } else {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Image", style = MaterialTheme.typography.labelMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Content - Notion-style editor
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { 
                    Text(
                        "Start writing...",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
                ),
                minLines = 15,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}

