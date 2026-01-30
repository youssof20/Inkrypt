package com.betterappsstudio.inkrypt.repository

import com.betterappsstudio.inkrypt.data.EncryptionUtil
import com.betterappsstudio.inkrypt.data.dao.JournalEntryDao
import com.betterappsstudio.inkrypt.data.dao.TemplateDao
import com.betterappsstudio.inkrypt.data.entity.JournalEntry
import com.betterappsstudio.inkrypt.data.entity.Template
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import javax.crypto.SecretKey

/**
 * Repository for journal entries and templates.
 * Handles encryption/decryption of data.
 */
class JournalRepository(
    private val journalEntryDao: JournalEntryDao,
    private val templateDao: TemplateDao,
    private val encryptionKey: SecretKey
) {
    fun getAllEntries(): Flow<List<DecryptedEntry>> {
        return journalEntryDao.getAllEntries()
            .map { entries ->
                entries.mapNotNull { entry -> safeDecryptEntry(entry) }
            }
            .flowOn(Dispatchers.IO)
    }
    
    suspend fun getEntryById(id: Long): DecryptedEntry? {
        val entry = journalEntryDao.getEntryById(id) ?: return null
        return safeDecryptEntry(entry)
    }
    
    suspend fun searchEntries(query: String): List<DecryptedEntry> {
        if (query.isBlank()) return emptyList()
        val entries = journalEntryDao.searchEntries(query.trim())
        return entries.mapNotNull { safeDecryptEntry(it) }
    }
    
    suspend fun insertEntry(entry: DecryptedEntry): Long {
        val encrypted = encryptEntry(entry)
        return journalEntryDao.insertEntry(encrypted)
    }
    
    suspend fun updateEntry(entry: DecryptedEntry) {
        val encrypted = encryptEntry(entry.copy(id = entry.id))
        journalEntryDao.updateEntry(encrypted)
    }
    
    suspend fun deleteEntry(entry: DecryptedEntry) {
        val encrypted = encryptEntry(entry.copy(id = entry.id))
        journalEntryDao.deleteEntry(encrypted)
    }
    
    fun getAllTemplates(): Flow<List<DecryptedTemplate>> {
        return templateDao.getAllTemplates()
            .map { templates ->
                templates.mapNotNull { safeDecryptTemplate(it) }
            }
            .flowOn(Dispatchers.IO)
    }
    
    suspend fun getTemplateById(id: Long): DecryptedTemplate? {
        val template = templateDao.getTemplateById(id) ?: return null
        return safeDecryptTemplate(template)
    }
    
    suspend fun insertTemplate(template: DecryptedTemplate): Long {
        val encrypted = encryptTemplate(template)
        return templateDao.insertTemplate(encrypted)
    }
    
    suspend fun updateTemplate(template: DecryptedTemplate) {
        val encrypted = encryptTemplate(template.copy(id = template.id))
        templateDao.updateTemplate(encrypted)
    }
    
    suspend fun deleteTemplate(template: DecryptedTemplate) {
        val encrypted = encryptTemplate(template.copy(id = template.id))
        templateDao.deleteTemplate(encrypted)
    }
    
    private suspend fun encryptEntry(entry: DecryptedEntry): JournalEntry {
        val encryptedTitle = EncryptionUtil.encrypt(entry.title, encryptionKey)
        val encryptedContent = EncryptionUtil.encrypt(entry.content, encryptionKey)
        val encryptedTags = EncryptionUtil.encrypt(entry.tags.joinToString(","), encryptionKey)
        val encryptedImagePaths = EncryptionUtil.encrypt(entry.imagePaths.joinToString(","), encryptionKey)
        val encryptedVoicePath = if (entry.voiceNotePath != null) {
            EncryptionUtil.encrypt(entry.voiceNotePath, encryptionKey)
        } else ""
        
        return JournalEntry(
            id = entry.id,
            title = encryptedTitle,
            content = encryptedContent,
            createdAt = entry.createdAt,
            updatedAt = entry.updatedAt,
            templateId = entry.templateId,
            tags = encryptedTags,
            imagePaths = encryptedImagePaths,
            voiceNotePath = encryptedVoicePath
        )
    }
    
    private suspend fun safeDecryptEntry(entry: JournalEntry): DecryptedEntry? {
        return try {
            decryptEntry(entry)
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun decryptEntry(entry: JournalEntry): DecryptedEntry {
        val title = EncryptionUtil.decrypt(entry.title, encryptionKey)
        val content = EncryptionUtil.decrypt(entry.content, encryptionKey)
        val tags = if (entry.tags.isNotEmpty()) {
            try {
                EncryptionUtil.decrypt(entry.tags, encryptionKey).split(",").filter { it.isNotBlank() }
            } catch (e: Exception) { emptyList() }
        } else emptyList()
        val imagePaths = if (entry.imagePaths.isNotEmpty()) {
            try {
                EncryptionUtil.decrypt(entry.imagePaths, encryptionKey).split(",").filter { it.isNotBlank() }
            } catch (e: Exception) { emptyList() }
        } else emptyList()
        val voicePath = if (entry.voiceNotePath.isNotEmpty()) {
            try {
                EncryptionUtil.decrypt(entry.voiceNotePath, encryptionKey)
            } catch (e: Exception) { null }
        } else null
        
        return DecryptedEntry(
            id = entry.id,
            title = title,
            content = content,
            createdAt = entry.createdAt,
            updatedAt = entry.updatedAt,
            templateId = entry.templateId,
            tags = tags,
            imagePaths = imagePaths,
            voiceNotePath = voicePath
        )
    }
    
    private suspend fun encryptTemplate(template: DecryptedTemplate): Template {
        val encryptedName = EncryptionUtil.encrypt(template.name, encryptionKey)
        val encryptedContent = EncryptionUtil.encrypt(template.content, encryptionKey)
        return Template(
            id = template.id,
            name = encryptedName,
            content = encryptedContent
        )
    }
    
    private suspend fun safeDecryptTemplate(template: Template): DecryptedTemplate? {
        return try {
            decryptTemplate(template)
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun decryptTemplate(template: Template): DecryptedTemplate {
        val name = EncryptionUtil.decrypt(template.name, encryptionKey)
        val content = EncryptionUtil.decrypt(template.content, encryptionKey)
        return DecryptedTemplate(
            id = template.id,
            name = name,
            content = content
        )
    }
    
    data class DecryptedEntry(
        val id: Long = 0,
        val title: String,
        val content: String,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val templateId: Long? = null,
        val tags: List<String> = emptyList(),
        val imagePaths: List<String> = emptyList(),
        val voiceNotePath: String? = null
    )
    
    data class DecryptedTemplate(
        val id: Long = 0,
        val name: String,
        val content: String
    )
}

