package com.example.drawingapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.drawingapp.data.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class DrawingRepositoryTest {
    private lateinit var drawingDao: DrawingDao
    private lateinit var db: AppDatabase
    private lateinit var repository: DrawingRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        drawingDao = db.drawingDao()
        repository = DrawingRepository(drawingDao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetDrawing() = runBlocking {
        val drawing = Drawing(name = "Test Drawing", serializedPaths = "[]", thumbnail = "")
        repository.insertDrawing(drawing)
        val allDrawings = repository.getAllDrawings()
        assertTrue(allDrawings.isNotEmpty())
        assertEquals("Test Drawing", allDrawings.first().name)
    }

    @Test
    @Throws(Exception::class)
    fun deleteDrawing() = runBlocking {
        val drawing = Drawing(name = "Test Drawing", serializedPaths = "[]", thumbnail = "")
        repository.insertDrawing(drawing)
        val insertedDrawing = repository.getAllDrawings().first()
        repository.deleteDrawing(insertedDrawing)
        val allDrawings = repository.getAllDrawings()
        assertTrue(allDrawings.isEmpty())
    }
}