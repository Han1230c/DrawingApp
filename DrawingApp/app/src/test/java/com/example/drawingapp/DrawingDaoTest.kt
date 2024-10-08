package com.example.drawingapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.drawingapp.data.AppDatabase
import com.example.drawingapp.data.Drawing
import com.example.drawingapp.data.DrawingDao
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
        // Create an in-memory database before each test
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        drawingDao = db.drawingDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        // Close the database after each test
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetDrawing() = runBlocking {
        // Test inserting a drawing and retrieving it by ID
        val drawing = Drawing(name = "Test Drawing", serializedPaths = "[]", thumbnail = "")
        drawingDao.insertDrawing(drawing)
        val retrievedDrawing = drawingDao.getDrawingById(1)
        assertNotNull(retrievedDrawing)  // Ensure the drawing is not null
        assertEquals("Test Drawing", retrievedDrawing?.name)  // Verify the name matches
    }

    @Test
    @Throws(Exception::class)
    fun getAllDrawings() = runBlocking {
        // Test retrieving all inserted drawings
        val drawing1 = Drawing(name = "Drawing 1", serializedPaths = "[]", thumbnail = "")
        val drawing2 = Drawing(name = "Drawing 2", serializedPaths = "[]", thumbnail = "")
        drawingDao.insertDrawing(drawing1)
        drawingDao.insertDrawing(drawing2)

        val allDrawings = drawingDao.getAllDrawings()
        assertEquals(2, allDrawings.size)  // Ensure there are exactly 2 drawings
    }

    @Test
    @Throws(Exception::class)
    fun deleteDrawing() = runBlocking {
        // Test deleting a drawing from the database
        val drawing = Drawing(name = "Test Drawing", serializedPaths = "[]", thumbnail = "")
        drawingDao.insertDrawing(drawing)

        val insertedDrawing = drawingDao.getAllDrawings().first()
        drawingDao.deleteDrawing(insertedDrawing)

        val allDrawings = drawingDao.getAllDrawings()
        assertTrue(allDrawings.isEmpty())  // Ensure the drawing has been deleted
    }
}