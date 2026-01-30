package com.betterappsstudio.inkrypt.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.Mac
import java.security.MessageDigest
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory

/**
 * Encryption utility using AES-GCM for symmetric encryption.
 * Uses PBKDF2 for key derivation from PIN.
 */
object EncryptionUtil {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val IV_SIZE = 12 // 96 bits for GCM
    private const val TAG_SIZE = 128 // 128 bits authentication tag
    private const val PBKDF2_ITERATIONS = 100000
    private const val SALT_SIZE = 16
    
    /**
     * Derives an encryption key from a PIN using PBKDF2.
     */
    fun deriveKeyFromPin(pin: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(
            pin.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_SIZE
        )
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
    
    /**
     * Generates a random salt for key derivation.
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_SIZE)
        SecureRandom().nextBytes(salt)
        return salt
    }
    
    /**
     * Encrypts plaintext using AES-GCM.
     */
    suspend fun encrypt(plaintext: String, key: SecretKey): String = withContext(Dispatchers.IO) {
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)
        
        val parameterSpec = GCMParameterSpec(TAG_SIZE, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)
        
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        
        // Combine IV + ciphertext
        val encrypted = ByteArray(IV_SIZE + ciphertext.size)
        System.arraycopy(iv, 0, encrypted, 0, IV_SIZE)
        System.arraycopy(ciphertext, 0, encrypted, IV_SIZE, ciphertext.size)
        
        Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }
    
    /**
     * Decrypts ciphertext using AES-GCM.
     */
    suspend fun decrypt(ciphertext: String, key: SecretKey): String = withContext(Dispatchers.IO) {
        val encrypted = Base64.decode(ciphertext, Base64.NO_WRAP)
        
        // Extract IV and ciphertext
        val iv = ByteArray(IV_SIZE)
        System.arraycopy(encrypted, 0, iv, 0, IV_SIZE)
        
        val ciphertextBytes = ByteArray(encrypted.size - IV_SIZE)
        System.arraycopy(encrypted, IV_SIZE, ciphertextBytes, 0, ciphertextBytes.size)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        val parameterSpec = GCMParameterSpec(TAG_SIZE, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)
        
        val plaintext = cipher.doFinal(ciphertextBytes)
        String(plaintext, Charsets.UTF_8)
    }
    
    /**
     * Verifies a PIN by attempting to decrypt a known value.
     */
    suspend fun verifyPin(pin: String, salt: ByteArray, encryptedTestValue: String): Boolean {
        return try {
            val key = deriveKeyFromPin(pin, salt)
            decrypt(encryptedTestValue, key)
            true
        } catch (e: Exception) {
            false
        }
    }
}

