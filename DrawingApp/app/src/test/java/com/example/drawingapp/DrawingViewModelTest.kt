package com.example.drawingapp

import android.graphics.Paint
import android.graphics.Path
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@ExperimentalCoroutinesApi
class DrawingViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = object : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(testDispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }

    @Mock
    private lateinit var drawingDao: DrawingDao

    @Mock
    private lateinit var allDrawingsObserver: Observer<List<Drawing>>

    @Mock
    private lateinit var loadedPathsObserver: Observer<List<DrawingView.PathData>>

    private lateinit var viewModel: DrawingViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = DrawingViewModel(drawingDao)
        viewModel.allDrawings.observeForever(allDrawingsObserver)
        viewModel.loadedPaths.observeForever(loadedPathsObserver)
    }

    @After
    fun tearDown() {
        viewModel.allDrawings.removeObserver(allDrawingsObserver)
        viewModel.loadedPaths.removeObserver(loadedPathsObserver)
    }

    @Test
    fun loadAllDrawings_success() = runTest {
        // Test loading all drawings from the database successfully
        val drawings = listOf(Drawing(1, "Test Drawing", "", ""))
        whenever(drawingDao.getAllDrawings()).thenReturn(drawings)

        viewModel.loadAllDrawings()

        advanceUntilIdle()
        verify(allDrawingsObserver).onChanged(drawings)  // Verify observer gets the correct data
    }

    @Test
    fun saveDrawing_success() = runTest {
        // Test saving a new drawing
        val drawingName = "New Drawing"
        val paths = listOf(DrawingView.PathData(Path(), Paint(), PenShape.ROUND))

        viewModel.saveDrawing(drawingName, paths)

        advanceUntilIdle()
        verify(drawingDao, times(1)).insertDrawing(any())  // Ensure the drawing is inserted
        verify(drawingDao, times(1)).getAllDrawings()  // Ensure getAllDrawings() is called
    }

    @Test
    fun loadDrawingById_success() = runTest {
        // Test loading a specific drawing by ID
        val drawingId = 1
        val drawing = Drawing(
            drawingId,
            "Test Drawing",
            "[{\"points\":[0,0,100,100],\"color\":0,\"strokeWidth\":5.0,\"alpha\":255,\"shape\":\"ROUND\"}]",
            ""
        )
        whenever(drawingDao.getDrawingById(drawingId)).thenReturn(drawing)

        viewModel.loadDrawingById(drawingId)

        advanceUntilIdle()
        verify(loadedPathsObserver).onChanged(check { paths ->
            assertNotNull(paths)  // Verify the paths are not null
            assertTrue(paths.isNotEmpty())  // Ensure the paths list is not empty
        })
    }

    @Test
    fun deleteDrawing_success() = runTest {
        // Test deleting a drawing
        val drawing = Drawing(1, "Test Drawing", "", "")

        viewModel.deleteDrawing(drawing)

        advanceUntilIdle()
        verify(drawingDao, times(1)).deleteDrawing(drawing)  // Verify the delete is called
        verify(drawingDao, times(1)).getAllDrawings()  // Ensure getAllDrawings() is called
    }

    @Test
    fun loadDrawingById_notFound() = runTest {
        // Test case where drawing is not found by ID
        val drawingId = 1
        whenever(drawingDao.getDrawingById(drawingId)).thenReturn(null)

        viewModel.loadDrawingById(drawingId)

        advanceUntilIdle()
        verify(loadedPathsObserver, never()).onChanged(any())  // Ensure no changes in observer
    }

    @Test
    fun saveDrawing_emptyName() = runTest {
        // Test that saving a drawing with an empty name doesn't save it
        val drawingName = ""
        val paths = listOf<DrawingView.PathData>()

        viewModel.saveDrawing(drawingName, paths)

        advanceUntilIdle()
        verify(drawingDao, never()).insertDrawing(any())  // Verify insert is not called
        verify(drawingDao, never()).getAllDrawings()  // Verify getAllDrawings() is not called
    }

    @Test
    fun loadAllDrawings_empty() = runTest {
        // Test loading an empty list of drawings
        whenever(drawingDao.getAllDrawings()).thenReturn(emptyList())

        viewModel.loadAllDrawings()

        advanceUntilIdle()
        verify(allDrawingsObserver).onChanged(emptyList())  // Verify empty list is returned
    }
}
