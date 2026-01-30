package com.betterappsstudio.inkrypt.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betterappsstudio.inkrypt.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Authenticated) {
            onAuthenticated()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = authState) {
            is AuthViewModel.AuthState.Initializing -> {
                CircularProgressIndicator()
            }
            is AuthViewModel.AuthState.SetupRequired -> {
                PinSetupScreen(
                    onPinSet = { pin ->
                        viewModel.setupPin(pin)
                    }
                )
            }
            is AuthViewModel.AuthState.Locked -> {
                PinEntryScreen(
                    viewModel = viewModel,
                    onAuthenticated = onAuthenticated
                )
            }
            is AuthViewModel.AuthState.Authenticated -> {
                // Will navigate via LaunchedEffect
            }
            is AuthViewModel.AuthState.Error -> {
                ErrorStateView(
                    message = state.message,
                    onTryAgain = { viewModel.clearError() }
                )
            }
        }
    }
}

@Composable
fun PinSetupScreen(onPinSet: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Welcome to Inkrypt",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Create a PIN to secure your journal",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = pin,
            onValueChange = {
                pin = it
                error = null
            },
            label = { Text("PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = confirmPin,
            onValueChange = {
                confirmPin = it
                error = null
            },
            label = { Text("Confirm PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Button(
            onClick = {
                when {
                    pin.length < 4 -> error = "PIN must be at least 4 digits"
                    pin.length > 16 -> error = "PIN must be at most 16 digits"
                    pin != confirmPin -> error = "PINs do not match"
                    else -> onPinSet(pin)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Set PIN")
        }
    }
}

@Composable
fun PinEntryScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Inkrypt",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Enter your PIN to unlock",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = pin,
            onValueChange = {
                pin = it
                error = null
            },
            label = { Text("PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Button(
            onClick = {
                if (pin.length < 4) {
                    error = "Please enter your PIN"
                } else {
                    viewModel.verifyPin(pin)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Unlock")
        }

        var showResetDialog by remember { mutableStateOf(false) }
        TextButton(onClick = { showResetDialog = true }) {
            Text("Forgot PIN?", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset app") },
                text = {
                    Text(
                        "This will delete all journal data and remove your PIN. " +
                        "You will need to set a new PIN. Data cannot be recovered."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showResetDialog = false
                            viewModel.resetApp()
                        }
                    ) {
                        Text("Reset", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
    
    LaunchedEffect(viewModel.authState) {
        val state = viewModel.authState.value
        if (state is AuthViewModel.AuthState.Error) {
            error = state.message
        }
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onTryAgain: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Button(onClick = onTryAgain) {
            Text("Try again")
        }
    }
}

