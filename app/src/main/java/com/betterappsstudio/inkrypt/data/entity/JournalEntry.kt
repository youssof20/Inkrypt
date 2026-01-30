package com.betterappsstudio.inkrypt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Journal entry entity stored encrypted in the database.
 * The content field contains encrypted text.
 */
@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String, // Encrypted
    val content: String, // Encrypted
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val templateId: Long? = null,
    val tags: String = "", // Encrypted, comma-separated
    val imagePaths: String = "", // Encrypted, comma-separated file paths
    val voiceNotePath: String = "" // Encrypted, path to voice note
) {
    fun getDate(): Date = Date(createdAt)
}

