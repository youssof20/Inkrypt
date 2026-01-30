package com.betterappsstudio.inkrypt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.betterappsstudio.inkrypt.InkryptApplication
import com.betterappsstudio.inkrypt.auth.AuthManager
import com.betterappsstudio.inkrypt.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.crypto.SecretKey

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authManager = AuthManager(application)
    private val app = application as InkryptApplication
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initializing)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    private var stateBeforeError: AuthState = AuthState.Locked
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        if (authManager.isPinSet()) {
            _authState.value = AuthState.Locked
        } else {
            _authState.value = AuthState.SetupRequired
        }
    }
    
    fun setupPin(pin: String) {
        viewModelScope.launch {
            stateBeforeError = AuthState.SetupRequired
            try {
                val key = authManager.setupPin(pin)
                initializeApp(key)
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to setup PIN")
            }
        }
    }

    fun verifyPin(pin: String) {
        viewModelScope.launch {
            stateBeforeError = AuthState.Locked
            try {
                val key = authManager.verifyPin(pin)
                if (key != null) {
                    initializeApp(key)
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Invalid PIN")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to verify PIN")
            }
        }
    }
    
    fun logout() {
        app.clearDatabase()
        _authState.value = AuthState.Locked
    }
    
    private fun initializeApp(key: SecretKey) {
        app.initializeDatabase(key)
    }
    
    fun isBiometricAvailable(): Boolean {
        return authManager.isBiometricAvailable()
    }
    
    fun isBiometricEnabled(): Boolean {
        return authManager.isBiometricEnabled()
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        authManager.setBiometricEnabled(enabled)
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = stateBeforeError
        }
    }

    /**
     * Resets the app: clears stored PIN and all journal data. User must set a new PIN.
     * Data cannot be recovered without the old PIN.
     */
    fun resetApp() {
        PreferencesManager(app).clearAll()
        app.clearDatabase()
        try {
            app.deleteDatabase("inkrypt_database")
        } catch (_: Exception) { }
        _authState.value = AuthState.SetupRequired
    }

    sealed class AuthState {
        object Initializing : AuthState()
        object SetupRequired : AuthState()
        object Locked : AuthState()
        object Authenticated : AuthState()
        data class Error(val message: String) : AuthState()
    }
}

