package com.betterappsstudio.inkrypt.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.betterappsstudio.inkrypt.data.EncryptionUtil
import com.betterappsstudio.inkrypt.data.PreferencesManager
import javax.crypto.SecretKey

/**
 * Manages authentication including PIN and biometric unlock.
 */
class AuthManager(private val context: Context) {
    private val prefs = PreferencesManager(context)
    
    /**
     * Checks if biometric authentication is available.
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    /**
     * Sets up a new PIN and derives encryption key.
     */
    suspend fun setupPin(pin: String): SecretKey {
        when {
            pin.isBlank() -> throw IllegalArgumentException("PIN is required")
            pin.length < 4 -> throw IllegalArgumentException("PIN must be at least 4 characters")
            pin.length > 16 -> throw IllegalArgumentException("PIN must be at most 16 characters")
        }
        val salt = EncryptionUtil.generateSalt()
        prefs.savePinSalt(salt)
        
        val key = EncryptionUtil.deriveKeyFromPin(pin, salt)
        
        // Store encrypted test value for verification
        val testValue = "inkrypt_verification"
        val encryptedTest = EncryptionUtil.encrypt(testValue, key)
        prefs.saveEncryptedTestValue(encryptedTest)
        prefs.setPinSet(true)
        
        return key
    }
    
    /**
     * Verifies PIN and returns encryption key if valid.
     */
    suspend fun verifyPin(pin: String): SecretKey? {
        if (pin.isBlank()) return null
        val salt = prefs.getPinSalt() ?: return null
        val encryptedTest = prefs.getEncryptedTestValue() ?: return null
        
        val key = EncryptionUtil.deriveKeyFromPin(pin, salt)
        
        return try {
            val decrypted = EncryptionUtil.decrypt(encryptedTest, key)
            if (decrypted == "inkrypt_verification") {
                key
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Checks if PIN is set.
     */
    fun isPinSet(): Boolean {
        return prefs.isPinSet()
    }
    
    /**
     * Checks if biometric is enabled.
     */
    fun isBiometricEnabled(): Boolean {
        return prefs.isBiometricEnabled()
    }
    
    /**
     * Sets biometric enabled preference.
     */
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.setBiometricEnabled(enabled)
    }
    
    /**
     * Shows biometric prompt and returns encryption key on success.
     */
    fun authenticateWithBiometric(
        activity: FragmentActivity,
        onSuccess: (SecretKey) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isBiometricAvailable()) {
            onError("Biometric authentication not available")
            return
        }
        
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    // Get the key from stored PIN
                    val salt = prefs.getPinSalt() ?: run {
                        onError("PIN not set")
                        return
                    }
                    
                    // We need the PIN to derive the key, but we can't get it from biometric
                    // So we'll need to store a derived key or use Android Keystore
                    // For now, we'll require PIN entry after biometric
                    onError("Please enter PIN")
                }
                
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    onError(errString.toString())
                }
                
                override fun onAuthenticationFailed() {
                    onError("Authentication failed")
                }
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Inkrypt")
            .setSubtitle("Use your fingerprint or face to unlock")
            .setNegativeButtonText("Cancel")
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
}

