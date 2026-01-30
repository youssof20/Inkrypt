package com.betterappsstudio.inkrypt.data.dao

import androidx.room.*
import com.betterappsstudio.inkrypt.data.entity.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY updatedAt DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>
    
    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): JournalEntry?
    
    @Query("SELECT * FROM journal_entries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    suspend fun searchEntries(query: String): List<JournalEntry>
    
    @Insert
    suspend fun insertEntry(entry: JournalEntry): Long
    
    @Update
    suspend fun updateEntry(entry: JournalEntry)
    
    @Delete
    suspend fun deleteEntry(entry: JournalEntry)
    
    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllEntries()
}

