import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.drawingapp.AppDatabase
import com.example.drawingapp.Drawing
import com.example.drawingapp.DrawingDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrawingDaoTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var drawingDao: DrawingDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        drawingDao = database.drawingDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetDrawing() = runBlocking {
        val drawing = Drawing(0, "Test Drawing", "[]", "")
        drawingDao.insertDrawing(drawing)

        val loadedDrawings = drawingDao.getAllDrawings()
        assertEquals(1, loadedDrawings.size)
        assertEquals("Test Drawing", loadedDrawings[0].name)
    }

    @Test
    fun deleteDrawing() = runBlocking {
        val drawing = Drawing(0, "Test Drawing", "[]", "")
        drawingDao.insertDrawing(drawing)

        var loadedDrawings = drawingDao.getAllDrawings()
        assertEquals(1, loadedDrawings.size)

        drawingDao.deleteDrawing(loadedDrawings[0])
        loadedDrawings = drawingDao.getAllDrawings()
        assertEquals(0, loadedDrawings.size)
    }
}