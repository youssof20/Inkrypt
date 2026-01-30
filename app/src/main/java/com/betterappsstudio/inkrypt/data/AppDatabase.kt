package com.betterappsstudio.inkrypt.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.betterappsstudio.inkrypt.data.dao.JournalEntryDao
import com.betterappsstudio.inkrypt.data.dao.TemplateDao
import com.betterappsstudio.inkrypt.data.entity.JournalEntry
import com.betterappsstudio.inkrypt.data.entity.Template
import net.sqlcipher.database.SupportFactory
import javax.crypto.SecretKey

@Database(
    entities = [JournalEntry::class, Template::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun templateDao(): TemplateDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context, encryptionKey: SecretKey): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val keyBytes = encryptionKey.encoded
                val passphrase = keyBytes.joinToString("") { "%02x".format(it) }
                
                val factory = SupportFactory(passphrase.toByteArray())
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inkrypt_database"
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration() // Allow destructive migration for schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

