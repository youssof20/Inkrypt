package com.betterappsstudio.inkrypt.export

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.betterappsstudio.inkrypt.data.AppDatabase
import com.betterappsstudio.inkrypt.data.EncryptionUtil
import com.betterappsstudio.inkrypt.repository.JournalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.crypto.SecretKey

@RunWith(AndroidJUnit4::class)
class ExportImportManagerTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: JournalRepository
    private lateinit var exportImportManager: ExportImportManager
    private lateinit var encryptionKey: SecretKey

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        val pin = "1234"
        val salt = EncryptionUtil.generateSalt()
        encryptionKey = EncryptionUtil.deriveKeyFromPin(pin, salt)

        database = AppDatabase.getDatabase(context, encryptionKey)
        repository = JournalRepository(
            database.journalEntryDao(),
            database.templateDao(),
            encryptionKey
        )

        exportImportManager = ExportImportManager(context, repository, encryptionKey)

        repository.insertEntry(
            JournalRepository.DecryptedEntry(
                title = "Test Entry 1",
                content = "This is test content 1"
            )
        )
        repository.insertEntry(
            JournalRepository.DecryptedEntry(
                title = "Test Entry 2",
                content = "This is test content 2"
            )
        )
    }

    @After
    fun tearDown() {
        AppDatabase.destroyInstance()
    }

    @Test
    fun testExportToMarkdown() = runBlocking {
        val mdFile = exportImportManager.exportToMarkdown()

        assertTrue(mdFile.exists())
        assertTrue(mdFile.name.endsWith(".md"))

        val content = mdFile.readText()
        assertTrue(content.contains("Inkrypt Export"))
        assertTrue(content.contains("Test Entry 1"))
        assertTrue(content.contains("Test Entry 2"))

        mdFile.delete()
    }

    @Test
    fun testExportToEncryptedZip() = runBlocking {
        val password = "testpassword"
        val zipFile = exportImportManager.exportToEncryptedZip(password)

        assertTrue(zipFile.exists())
        assertTrue(zipFile.name.endsWith(".zip"))

        zipFile.delete()
    }

    @Test
    fun testImportFromMarkdown() = runBlocking {
        val mdFile = exportImportManager.exportToMarkdown()

        repository.getAllEntries().first().forEach {
            repository.deleteEntry(it)
        }

        val importedCount = exportImportManager.importFromMarkdown(mdFile)

        assertEquals(2, importedCount)

        val entries = repository.getAllEntries().first()
        assertEquals(2, entries.size)

        mdFile.delete()
    }

    @Test
    fun testImportFromEncryptedZip() = runBlocking {
        val password = "testpassword"

        val zipFile = exportImportManager.exportToEncryptedZip(password)

        repository.getAllEntries().first().forEach {
            repository.deleteEntry(it)
        }

        val importedCount = exportImportManager.importFromEncryptedZip(zipFile, password)

        assertTrue(importedCount > 0)

        zipFile.delete()
    }
}
