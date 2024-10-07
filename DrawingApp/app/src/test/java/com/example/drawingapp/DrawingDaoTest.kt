package com.example.drawingapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DrawingDaoTest {
    private lateinit var drawingDao: DrawingDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        drawingDao = db.drawingDao()
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
        drawingDao.insertDrawing(drawing)
        val retrievedDrawing = drawingDao.getDrawingById(1)
        assertNotNull(retrievedDrawing)
        assertEquals("Test Drawing", retrievedDrawing?.name)
    }

    @Test
    @Throws(Exception::class)
    fun getAllDrawings() = runBlocking {
        val drawing1 = Drawing(name = "Drawing 1", serializedPaths = "[]", thumbnail = "")
        val drawing2 = Drawing(name = "Drawing 2", serializedPaths = "[]", thumbnail = "")
        drawingDao.insertDrawing(drawing1)
        drawingDao.insertDrawing(drawing2)

        val allDrawings = drawingDao.getAllDrawings()
        assertEquals(2, allDrawings.size)
    }

    @Test
    @Throws(Exception::class)
    fun deleteDrawing() = runBlocking {
        val drawing = Drawing(name = "Test Drawing", serializedPaths = "[]", thumbnail = "")
        drawingDao.insertDrawing(drawing)

        val insertedDrawing = drawingDao.getAllDrawings().first()
        drawingDao.deleteDrawing(insertedDrawing)

        val allDrawings = drawingDao.getAllDrawings()
        assertTrue(allDrawings.isEmpty())
    }
}