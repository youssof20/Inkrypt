package com.betterappsstudio.inkrypt

import android.app.Application
import com.betterappsstudio.inkrypt.data.AppDatabase
import javax.crypto.SecretKey

class InkryptApplication : Application() {
    var encryptionKey: SecretKey? = null
        private set
    
    var database: AppDatabase? = null
        private set
    
    fun initializeDatabase(key: SecretKey) {
        encryptionKey = key
        database = AppDatabase.getDatabase(this, key)
    }
    
    fun clearDatabase() {
        database = null
        encryptionKey = null
        AppDatabase.destroyInstance()
    }
}

