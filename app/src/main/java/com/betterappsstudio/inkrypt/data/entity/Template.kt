package com.betterappsstudio.inkrypt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Template entity for journal entry templates.
 * Content is stored encrypted.
 */
@Entity(tableName = "templates")
data class Template(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // Encrypted
    val content: String // Encrypted
)

