package com.betterappsstudio.inkrypt.export

import android.content.Context
import com.betterappsstudio.inkrypt.data.EncryptionUtil
import com.betterappsstudio.inkrypt.repository.JournalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.SecretKey

/**
 * Manages export and import of journal entries.
 * Supports encrypted .zip and plain .md formats.
 */
class ExportImportManager(
    private val context: Context,
    private val repository: JournalRepository,
    private val encryptionKey: SecretKey
) {
    
    /**
     * Exports all entries to an encrypted ZIP file.
     */
    suspend fun exportToEncryptedZip(password: String): File = withContext(Dispatchers.IO) {
        if (password.isBlank()) throw IllegalArgumentException("Export password cannot be empty")
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val zipFile = File(baseDir, "inkrypt_export_$timestamp.zip")
        
        val tempDir = File(context.cacheDir, "export_temp_$timestamp")
        tempDir.mkdirs()
        
        try {
            // Export entries as markdown files
            val entryList = repository.getAllEntries().first()
            
            entryList.forEach { entry ->
                val entryFile = File(tempDir, "entry_${entry.id}.md")
                FileWriter(entryFile).use { writer ->
                    writer.write("# ${entry.title}\n\n")
                    writer.write("Created: ${formatDate(entry.createdAt)}\n")
                    writer.write("Updated: ${formatDate(entry.updatedAt)}\n\n")
                    writer.write(entry.content)
                }
            }
            
            // Create encrypted ZIP
            val zipParameters = ZipParameters().apply {
                isEncryptFiles = true
                encryptionMethod = EncryptionMethod.AES
            }
            
            val zip = ZipFile(zipFile, password.toCharArray())
            tempDir.listFiles()?.forEach { file ->
                zip.addFile(file, zipParameters)
            }
            
            zipFile
        } finally {
            // Clean up temp directory
            tempDir.deleteRecursively()
        }
    }
    
    /**
     * Exports all entries to a plain markdown file.
     */
    suspend fun exportToMarkdown(): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val mdFile = File(baseDir, "inkrypt_export_$timestamp.md")
        
        FileWriter(mdFile).use { writer ->
            writer.write("# Inkrypt Export\n\n")
            writer.write("Exported: ${formatDate(System.currentTimeMillis())}\n\n")
            writer.write("---\n\n")
            
            val entryList = repository.getAllEntries().first()
            entryList.forEach { entry ->
                writer.write("## ${entry.title}\n\n")
                writer.write("**Created:** ${formatDate(entry.createdAt)}\n")
                writer.write("**Updated:** ${formatDate(entry.updatedAt)}\n\n")
                writer.write("${entry.content}\n\n")
                writer.write("---\n\n")
            }
        }
        
        mdFile
    }
    
    /**
     * Imports entries from an encrypted ZIP file.
     */
    suspend fun importFromEncryptedZip(zipFile: File, password: String): Int = withContext(Dispatchers.IO) {
        if (!zipFile.exists() || !zipFile.canRead()) return@withContext -2
        if (password.isBlank()) return@withContext -1
        val tempDir = File(context.cacheDir, "import_temp_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        
        try {
            val zip = ZipFile(zipFile, password.toCharArray())
            try {
                zip.extractAll(tempDir.absolutePath)
            } catch (e: Exception) {
                tempDir.deleteRecursively()
                return@withContext -1
            }
            
            var importedCount = 0
            tempDir.listFiles { _, name -> name.endsWith(".md") }?.forEach { file ->
                val content = file.readText()
                val lines = content.lines()
                
                if (lines.isNotEmpty()) {
                    val title = lines[0].removePrefix("# ").trim()
                    val entryContent = lines.dropWhile { it.isBlank() || it.startsWith("#") || it.startsWith("Created:") || it.startsWith("Updated:") }
                        .joinToString("\n")
                    
                    val entry = JournalRepository.DecryptedEntry(
                        title = title,
                        content = entryContent,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    repository.insertEntry(entry)
                    importedCount++
                }
            }
            
            importedCount
        } finally {
            tempDir.deleteRecursively()
        }
    }
    
    /**
     * Imports entries from a plain markdown file.
     */
    suspend fun importFromMarkdown(mdFile: File): Int = withContext(Dispatchers.IO) {
        if (!mdFile.exists() || !mdFile.canRead()) return@withContext -2
        val content = mdFile.readText()
        val sections = content.split("---")
        
        var importedCount = 0
        
        sections.forEach { section ->
            val lines = section.lines()
            if (lines.isNotEmpty() && lines[0].startsWith("##")) {
                val title = lines[0].removePrefix("##").trim()
                val entryContent = lines.drop(1)
                    .dropWhile { it.isBlank() || it.startsWith("**") }
                    .joinToString("\n")
                    .trim()
                
                if (title.isNotEmpty() && entryContent.isNotEmpty()) {
                    val entry = JournalRepository.DecryptedEntry(
                        title = title,
                        content = entryContent,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    repository.insertEntry(entry)
                    importedCount++
                }
            }
        }
        
        importedCount
    }
    
    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}

