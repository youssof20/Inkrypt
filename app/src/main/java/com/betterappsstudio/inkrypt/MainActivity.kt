package com.betterappsstudio.inkrypt

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.betterappsstudio.inkrypt.data.PreferencesManager
import com.betterappsstudio.inkrypt.export.ExportImportManager
import com.betterappsstudio.inkrypt.repository.JournalRepository
import com.betterappsstudio.inkrypt.ui.screens.*
import com.betterappsstudio.inkrypt.ui.theme.InkryptTheme
import com.betterappsstudio.inkrypt.viewmodel.AuthViewModel
import com.betterappsstudio.inkrypt.viewmodel.JournalViewModel
import com.betterappsstudio.inkrypt.viewmodel.SettingsViewModel
import com.betterappsstudio.inkrypt.viewmodel.TemplateViewModel
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleShareIntent(intent)
        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                PreferencesManager.THEME_DARK -> true
                PreferencesManager.THEME_LIGHT -> false
                else -> isSystemInDarkTheme()
            }
            InkryptTheme(
                darkTheme = darkTheme,
                dynamicColor = (themeMode == PreferencesManager.THEME_SYSTEM)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InkryptApp(
                        authViewModel = authViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }
    
    private fun handleShareIntent(intent: android.content.Intent) {
        if (android.content.Intent.ACTION_SEND == intent.action) {
            val sharedText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT)
            if (sharedText != null) {
                // Store shared text to be used when creating new entry
                // This will be handled in the EntryEditorScreen
                // For now, we'll just store it in a way that can be accessed
                // In a production app, you might use a ViewModel or SharedPreferences
            }
        }
    }
}

@Composable
fun InkryptApp(
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as InkryptApplication
    
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Authenticated -> {
                if (navController.currentDestination?.route != "entry_list") {
                    navController.navigate("entry_list") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            }
            is AuthViewModel.AuthState.Locked,
            is AuthViewModel.AuthState.SetupRequired -> {
                if (navController.currentDestination?.route != "auth") {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("auth") {
            AuthScreen(
                viewModel = authViewModel,
                onAuthenticated = {
                    navController.navigate("entry_list") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        
        composable("entry_list") {
            val database = app.database
            if (database != null && app.encryptionKey != null) {
                val repository = JournalRepository(
                    database.journalEntryDao(),
                    database.templateDao(),
                    app.encryptionKey!!
                )
                val journalViewModel = remember {
                    JournalViewModel(repository)
                }
                val entries by journalViewModel.entries.collectAsStateWithLifecycle()
                val searchResults by journalViewModel.searchResults.collectAsStateWithLifecycle()
                val isSearching by journalViewModel.isSearching.collectAsStateWithLifecycle()
                
                val displayedEntries = if (isSearching) searchResults else entries
                
                EntryListScreen(
                    entries = displayedEntries,
                    onEntryClick = { id ->
                        navController.navigate("entry_editor/$id")
                    },
                    onNewEntry = {
                        navController.navigate("entry_editor/0")
                    },
                    onSearch = { query ->
                        journalViewModel.search(query)
                    },
                    onOpenSettings = {
                        navController.navigate("settings")
                    },
                    onOpenTemplates = {
                        navController.navigate("templates")
                    },
                    onLogout = {
                        authViewModel.logout()
                    }
                )
            }
        }
        
        composable("entry_editor/{entryId}") { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")?.toLongOrNull() ?: 0L
            val database = app.database
            if (database != null && app.encryptionKey != null) {
                val repository = JournalRepository(
                    database.journalEntryDao(),
                    database.templateDao(),
                    app.encryptionKey!!
                )
                val journalViewModel = remember {
                    JournalViewModel(repository)
                }
                
                val context = androidx.compose.ui.platform.LocalContext.current
                
                val entry = remember(entryId) {
                    if (entryId > 0) {
                        kotlinx.coroutines.runBlocking {
                            journalViewModel.getEntry(entryId)
                        }
                    } else {
                        null
                    }
                }
                
                val mediaManager = remember {
                    com.betterappsstudio.inkrypt.data.MediaManager(
                        context = context,
                        encryptionKey = app.encryptionKey!!
                    )
                }
                
                com.betterappsstudio.inkrypt.ui.screens.EnhancedEntryEditorScreen(
                    entry = entry,
                    mediaManager = mediaManager,
                    onSave = { savedEntry ->
                        journalViewModel.saveEntry(savedEntry)
                    },
                    onDelete = { deletedEntry ->
                        journalViewModel.deleteEntry(deletedEntry)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable("templates") {
            val database = app.database
            if (database != null && app.encryptionKey != null) {
                val repository = JournalRepository(
                    database.journalEntryDao(),
                    database.templateDao(),
                    app.encryptionKey!!
                )
                val templateViewModel = remember {
                    TemplateViewModel(repository)
                }
                val templates by templateViewModel.templates.collectAsStateWithLifecycle()

                TemplatesScreen(
                    templates = templates,
                    onTemplateClick = { template ->
                        navController.navigate("entry_editor/0?template=${template.id}")
                    },
                    onNewTemplate = {
                        navController.navigate("template_editor/0")
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("template_editor/{templateId}") { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId")?.toLongOrNull() ?: 0L
            val database = app.database
            if (database != null && app.encryptionKey != null) {
                val repository = JournalRepository(
                    database.journalEntryDao(),
                    database.templateDao(),
                    app.encryptionKey!!
                )
                val templateViewModel = remember {
                    TemplateViewModel(repository)
                }
                val template = remember(templateId) {
                    if (templateId > 0) {
                        kotlinx.coroutines.runBlocking {
                            templateViewModel.getTemplate(templateId)
                        }
                    } else null
                }
                TemplateEditorScreen(
                    template = template,
                    onSave = { t ->
                        templateViewModel.saveTemplate(t)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("settings") {
            val database = app.database
            val key = app.encryptionKey
            if (database != null && key != null) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val repository = remember {
                    JournalRepository(
                        database.journalEntryDao(),
                        database.templateDao(),
                        key
                    )
                }
                val exportImportManager = remember(repository) {
                    ExportImportManager(context, repository, key)
                }
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
                val biometricEnabled by settingsViewModel.biometricEnabled.collectAsStateWithLifecycle()

                var pendingImportFile by remember { mutableStateOf<File?>(null) }
                var showImportZipPassword by remember { mutableStateOf(false) }
                var importZipPassword by remember { mutableStateOf("") }

                val importLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri: Uri? ->
                    if (uri == null) return@rememberLauncherForActivityResult
                    val name = run {
                        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (idx >= 0) cursor.getString(idx) else "import"
                            } else "import"
                        } ?: "import"
                    }
                    val ext = if (name.contains(".")) name.substringAfterLast(".").lowercase() else ""
                    val file = File(context.cacheDir, "import_${System.currentTimeMillis()}.$ext")
                    try {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output -> input.copyTo(output) }
                        }
                        if (ext == "zip") {
                            pendingImportFile = file
                            showImportZipPassword = true
                        } else if (ext == "md") {
                            scope.launch {
                                val count = exportImportManager.importFromMarkdown(file)
                                file.delete()
                                snackbarHostState.showSnackbar(
                                    if (count >= 0) "Imported $count entries" else "Import failed"
                                )
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Use a .md or .zip file")
                            }
                            file.delete()
                        }
                    } catch (_: Exception) {
                        file.delete()
                        scope.launch { snackbarHostState.showSnackbar("Import failed") }
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { _ ->
                    SettingsScreen(
                        themeMode = themeMode,
                        onThemeModeChange = { settingsViewModel.setThemeMode(it) },
                        biometricAvailable = settingsViewModel.isBiometricAvailable,
                        biometricEnabled = biometricEnabled,
                        onBiometricEnabledChange = { settingsViewModel.setBiometricEnabled(it) },
                        onExportMarkdown = {
                            scope.launch {
                                try {
                                    val file = exportImportManager.exportToMarkdown()
                                    snackbarHostState.showSnackbar("Saved: ${file.absolutePath}")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Export failed")
                                }
                            }
                        },
                        onExportZip = { password ->
                            scope.launch {
                                try {
                                    val file = exportImportManager.exportToEncryptedZip(password)
                                    snackbarHostState.showSnackbar("Saved: ${file.absolutePath}")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(e.message ?: "Export failed")
                                }
                            }
                        },
                        onImport = { importLauncher.launch(arrayOf("*/*")) },
                        onBack = { navController.popBackStack() }
                    )
                }

                if (showImportZipPassword) {
                    AlertDialog(
                        onDismissRequest = {
                            showImportZipPassword = false
                            importZipPassword = ""
                            pendingImportFile?.delete()
                            pendingImportFile = null
                        },
                        title = { Text("Import ZIP") },
                        text = {
                            OutlinedTextField(
                                value = importZipPassword,
                                onValueChange = { importZipPassword = it },
                                label = { Text("Password") },
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showImportZipPassword = false
                                    val file = pendingImportFile
                                    val pwd = importZipPassword
                                    pendingImportFile = null
                                    importZipPassword = ""
                                    if (file != null && pwd.isNotBlank()) {
                                        scope.launch {
                                            val count = exportImportManager.importFromEncryptedZip(file, pwd)
                                            file.delete()
                                            snackbarHostState.showSnackbar(
                                                when {
                                                    count >= 0 -> "Imported $count entries"
                                                    count == -1 -> "Wrong password"
                                                    else -> "Import failed"
                                                }
                                            )
                                        }
                                    }
                                }
                            ) {
                                Text("Import")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showImportZipPassword = false
                                    importZipPassword = ""
                                    pendingImportFile?.delete()
                                    pendingImportFile = null
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}
