package com.betterappsstudio.inkrypt.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.betterappsstudio.inkrypt.data.EncryptionUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.crypto.SecretKey

/**
 * Manages media files (images, voice notes) with encryption.
 */
class MediaManager(private val context: Context, private val encryptionKey: SecretKey) {
    
    private val mediaDir: File = File(context.filesDir, "encrypted_media")
    private val imagesDir: File = File(mediaDir, "images")
    private val voiceNotesDir: File = File(mediaDir, "voice_notes")
    private val thumbnailsDir: File = File(mediaDir, "thumbnails")
    
    init {
        mediaDir.mkdirs()
        imagesDir.mkdirs()
        voiceNotesDir.mkdirs()
        thumbnailsDir.mkdirs()
    }
    
    /**
     * Saves an image from URI and returns the encrypted file path.
     */
    suspend fun saveImage(uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val imageBytes = inputStream?.readBytes() ?: throw Exception("Failed to read image")
        inputStream?.close()
        
        // Create encrypted file
        val fileName = "img_${System.currentTimeMillis()}.enc"
        val encryptedFile = File(imagesDir, fileName)
        
        // Encrypt image bytes (convert to Base64 string first)
        val base64String = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
        val encryptedString = EncryptionUtil.encrypt(base64String, encryptionKey)
        encryptedFile.writeText(encryptedString)
        
        // Create thumbnail
        createThumbnail(imageBytes, fileName)
        
        encryptedFile.absolutePath
    }
    
    /**
     * Retrieves and decrypts an image.
     */
    suspend fun getImage(imagePath: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val encryptedFile = File(imagePath)
            if (!encryptedFile.exists()) return@withContext null
            
            val encryptedString = encryptedFile.readText()
            val decryptedString = EncryptionUtil.decrypt(encryptedString, encryptionKey)
            val decryptedBytes = android.util.Base64.decode(decryptedString, android.util.Base64.NO_WRAP)
            
            BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Gets thumbnail path for an image.
     */
    fun getThumbnailPath(imagePath: String): String {
        val fileName = File(imagePath).nameWithoutExtension
        return File(thumbnailsDir, "${fileName}_thumb.jpg").absolutePath
    }
    
    /**
     * Creates a thumbnail from image bytes.
     */
    private suspend fun createThumbnail(imageBytes: ByteArray, originalFileName: String) = withContext(Dispatchers.IO) {
        try {
            val originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val thumbnail = Bitmap.createScaledBitmap(originalBitmap, 200, 200, true)
            
            val thumbnailFile = File(thumbnailsDir, "${File(originalFileName).nameWithoutExtension}_thumb.jpg")
            FileOutputStream(thumbnailFile).use { out ->
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
        } catch (e: Exception) {
            // Thumbnail creation failed, continue without it
        }
    }
    
    /**
     * Starts recording a voice note and returns the MediaRecorder instance and temp file path.
     */
    fun startVoiceRecording(): Pair<MediaRecorder, String> {
        val tempFile = File(context.cacheDir, "temp_voice_${System.currentTimeMillis()}.3gp")
        
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(tempFile.absolutePath)
            prepare()
            start()
        }
        
        return Pair(recorder, tempFile.absolutePath)
    }
    
    /**
     * Stops recording and encrypts the voice note.
     */
    suspend fun stopVoiceRecording(recorder: MediaRecorder, tempPath: String): String = withContext(Dispatchers.IO) {
        try {
            recorder.stop()
            recorder.release()
            
            val tempFile = File(tempPath)
            val audioBytes = tempFile.readBytes()
            
            // Encrypt audio (convert to Base64 string first)
            val base64String = android.util.Base64.encodeToString(audioBytes, android.util.Base64.NO_WRAP)
            val encryptedString = EncryptionUtil.encrypt(base64String, encryptionKey)
            val encryptedFile = File(voiceNotesDir, "voice_${System.currentTimeMillis()}.enc")
            encryptedFile.writeText(encryptedString)
            
            // Delete temp file
            tempFile.delete()
            
            encryptedFile.absolutePath
        } catch (e: Exception) {
            File(tempPath).delete()
            throw e
        }
    }
    
    /**
     * Gets decrypted voice note file path for playback.
     */
    suspend fun getVoiceNoteFile(voicePath: String): File? = withContext(Dispatchers.IO) {
        try {
            val encryptedFile = File(voicePath)
            if (!encryptedFile.exists()) return@withContext null
            
            val encryptedString = encryptedFile.readText()
            val decryptedString = EncryptionUtil.decrypt(encryptedString, encryptionKey)
            val decryptedBytes = android.util.Base64.decode(decryptedString, android.util.Base64.NO_WRAP)
            
            val tempFile = File(context.cacheDir, "temp_voice_${System.currentTimeMillis()}.3gp")
            tempFile.writeBytes(decryptedBytes)
            
            tempFile
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Deletes a media file.
     */
    suspend fun deleteMedia(path: String) = withContext(Dispatchers.IO) {
        try {
            File(path).delete()
            // Also delete thumbnail if it's an image
            if (path.contains("images")) {
                val thumbnailPath = getThumbnailPath(path)
                File(thumbnailPath).delete()
            }
        } catch (e: Exception) {
            // Ignore deletion errors
        }
    }
    
}

