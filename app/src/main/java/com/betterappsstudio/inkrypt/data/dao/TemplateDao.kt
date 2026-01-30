package com.betterappsstudio.inkrypt.data.dao

import androidx.room.*
import com.betterappsstudio.inkrypt.data.entity.Template
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY name")
    fun getAllTemplates(): Flow<List<Template>>
    
    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): Template?
    
    @Insert
    suspend fun insertTemplate(template: Template): Long
    
    @Update
    suspend fun updateTemplate(template: Template)
    
    @Delete
    suspend fun deleteTemplate(template: Template)
}

