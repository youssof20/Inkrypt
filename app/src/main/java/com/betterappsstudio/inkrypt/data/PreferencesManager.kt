package com.betterappsstudio.inkrypt.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64

/**
 * Manages app preferences including PIN salt and encrypted test value.
 */
class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("inkrypt_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_ENCRYPTED_TEST_VALUE = "encrypted_test_value"
        private const val KEY_PIN_SET = "pin_set"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_THEME_MODE = "theme_mode"
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }
    
    fun getPinSalt(): ByteArray? {
        val saltBase64 = prefs.getString(KEY_PIN_SALT, null) ?: return null
        return Base64.decode(saltBase64, Base64.DEFAULT)
    }
    
    fun savePinSalt(salt: ByteArray) {
        prefs.edit()
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.DEFAULT))
            .apply()
    }
    
    fun getEncryptedTestValue(): String? {
        return prefs.getString(KEY_ENCRYPTED_TEST_VALUE, null)
    }
    
    fun saveEncryptedTestValue(value: String) {
        prefs.edit()
            .putString(KEY_ENCRYPTED_TEST_VALUE, value)
            .apply()
    }
    
    fun isPinSet(): Boolean {
        return prefs.getBoolean(KEY_PIN_SET, false)
    }
    
    fun setPinSet(value: Boolean) {
        prefs.edit()
            .putBoolean(KEY_PIN_SET, value)
            .apply()
    }
    
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
    
    fun setBiometricEnabled(value: Boolean) {
        prefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, value)
            .apply()
    }

    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}

