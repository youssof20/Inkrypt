package com.betterappsstudio.inkrypt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.betterappsstudio.inkrypt.auth.AuthManager
import com.betterappsstudio.inkrypt.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesManager(application)
    private val authManager = AuthManager(application)

    private val _themeMode = MutableStateFlow(prefs.getThemeMode())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(prefs.isBiometricEnabled())
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    val isBiometricAvailable: Boolean
        get() = authManager.isBiometricAvailable()

    fun setThemeMode(mode: String) {
        prefs.setThemeMode(mode)
        _themeMode.value = mode
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.setBiometricEnabled(enabled)
        _biometricEnabled.value = enabled
    }

    fun refreshFromPrefs() {
        _themeMode.value = prefs.getThemeMode()
        _biometricEnabled.value = prefs.isBiometricEnabled()
    }
}
