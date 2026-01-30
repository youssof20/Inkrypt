package com.betterappsstudio.inkrypt.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class EncryptionUtilTest {
    
    @Test
    fun testKeyDerivation() {
        val pin = "1234"
        val salt = EncryptionUtil.generateSalt()
        
        val key1 = EncryptionUtil.deriveKeyFromPin(pin, salt)
        val key2 = EncryptionUtil.deriveKeyFromPin(pin, salt)
        
        // Same PIN and salt should produce same key
        assertArrayEquals(key1.encoded, key2.encoded)
    }
    
    @Test
    fun testKeyDerivationDifferentSalts() {
        val pin = "1234"
        val salt1 = EncryptionUtil.generateSalt()
        val salt2 = EncryptionUtil.generateSalt()
        
        val key1 = EncryptionUtil.deriveKeyFromPin(pin, salt1)
        val key2 = EncryptionUtil.deriveKeyFromPin(pin, salt2)
        
        // Different salts should produce different keys
        assertFalse(key1.encoded.contentEquals(key2.encoded))
    }
    
    @Test
    fun testEncryptionDecryption() = runBlocking {
        val pin = "1234"
        val salt = EncryptionUtil.generateSalt()
        val key = EncryptionUtil.deriveKeyFromPin(pin, salt)
        
        val plaintext = "This is a test message"
        val ciphertext = EncryptionUtil.encrypt(plaintext, key)
        
        assertNotEquals(plaintext, ciphertext)
        
        val decrypted = EncryptionUtil.decrypt(ciphertext, key)
        assertEquals(plaintext, decrypted)
    }
    
    @Test
    fun testEncryptionDecryptionDifferentKeys() = runBlocking {
        val pin1 = "1234"
        val pin2 = "5678"
        val salt = EncryptionUtil.generateSalt()
        
        val key1 = EncryptionUtil.deriveKeyFromPin(pin1, salt)
        val key2 = EncryptionUtil.deriveKeyFromPin(pin2, salt)
        
        val plaintext = "This is a test message"
        val ciphertext = EncryptionUtil.encrypt(plaintext, key1)
        
        // Should fail to decrypt with wrong key
        try {
            val decrypted = EncryptionUtil.decrypt(ciphertext, key2)
            fail("Should have thrown an exception")
        } catch (e: Exception) {
            // Expected
        }
    }
    
    @Test
    fun testPinVerification() = runBlocking {
        val pin = "1234"
        val salt = EncryptionUtil.generateSalt()
        val key = EncryptionUtil.deriveKeyFromPin(pin, salt)
        
        val testValue = "inkrypt_verification"
        val encryptedTest = EncryptionUtil.encrypt(testValue, key)
        
        val isValid = EncryptionUtil.verifyPin(pin, salt, encryptedTest)
        assertTrue(isValid)
        
        val isValidWrong = EncryptionUtil.verifyPin("wrong", salt, encryptedTest)
        assertFalse(isValidWrong)
    }
}

