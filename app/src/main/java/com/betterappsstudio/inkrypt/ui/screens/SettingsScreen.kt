package com.betterappsstudio.inkrypt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.betterappsstudio.inkrypt.data.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    biometricAvailable: Boolean,
    biometricEnabled: Boolean,
    onBiometricEnabledChange: (Boolean) -> Unit,
    onExportMarkdown: () -> Unit,
    onExportZip: (password: String) -> Unit,
    onImport: () -> Unit,
    onBack: () -> Unit
) {
    var showZipPasswordDialog by remember { mutableStateOf(false) }
    var zipPassword by remember { mutableStateOf("") }
    var zipPasswordError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                "Appearance",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            listOf(
                Triple(PreferencesManager.THEME_SYSTEM, "System", "Follow device"),
                Triple(PreferencesManager.THEME_LIGHT, "Light", null),
                Triple(PreferencesManager.THEME_DARK, "Dark", null)
            ).forEach { (value, label, subtitle) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeModeChange(value) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                        if (subtitle != null) {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (themeMode == value) {
                        RadioButton(selected = true, onClick = null)
                    }
                }
            }

            if (biometricAvailable) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Security",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Use biometric unlock",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = onBiometricEnabledChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Data",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExportMarkdown() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Export as Markdown", style = MaterialTheme.typography.bodyLarge)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showZipPasswordDialog = true }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Export as encrypted ZIP", style = MaterialTheme.typography.bodyLarge)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onImport() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Import from file", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "About",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            val appContext = LocalContext.current.applicationContext
            Text(
                text = remember(appContext) {
                    val info = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
                    @Suppress("DEPRECATION")
                    "Inkrypt ${info.versionName ?: "?"} (${info.versionCode})"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showZipPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showZipPasswordDialog = false; zipPasswordError = null },
            title = { Text("Export as ZIP") },
            text = {
                Column {
                    OutlinedTextField(
                        value = zipPassword,
                        onValueChange = { zipPassword = it; zipPasswordError = null },
                        label = { Text("Password") },
                        isError = zipPasswordError != null,
                        supportingText = zipPasswordError?.let { { Text(it) } },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when {
                            zipPassword.isBlank() -> zipPasswordError = "Enter a password"
                            zipPassword.length < 4 -> zipPasswordError = "At least 4 characters"
                            else -> {
                                onExportZip(zipPassword)
                                showZipPasswordDialog = false
                                zipPassword = ""
                                zipPasswordError = null
                            }
                        }
                    }
                ) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showZipPasswordDialog = false; zipPasswordError = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
