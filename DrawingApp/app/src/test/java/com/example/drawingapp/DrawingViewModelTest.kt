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
        // Given
        val drawings = listOf(Drawing(1, "Test Drawing", "", ""))
        whenever(drawingDao.getAllDrawings()).thenReturn(drawings)

        // When
        viewModel.loadAllDrawings()

        // Then
        advanceUntilIdle()
        verify(allDrawingsObserver).onChanged(drawings)
    }

    @Test
    fun saveDrawing_success() = runTest {
        // Given
        val drawingName = "New Drawing"
        val paths = listOf(DrawingView.PathData(Path(), Paint(), PenShape.ROUND))

        // When
        viewModel.saveDrawing(drawingName, paths)

        // Then
        advanceUntilIdle()
        verify(drawingDao, times(1)).insertDrawing(any())
        verify(drawingDao, times(1)).getAllDrawings()
    }

    @Test
    fun loadDrawingById_success() = runTest {
        // Given
        val drawingId = 1
        val drawing = Drawing(
            drawingId,
            "Test Drawing",
            "[{\"points\":[0,0,100,100],\"color\":0,\"strokeWidth\":5.0,\"alpha\":255,\"shape\":\"ROUND\"}]",
            ""
        )
        whenever(drawingDao.getDrawingById(drawingId)).thenReturn(drawing)

        // When
        viewModel.loadDrawingById(drawingId)

        // Then
        advanceUntilIdle()
        verify(loadedPathsObserver).onChanged(check { paths ->
            assertNotNull(paths)
            assertTrue(paths.isNotEmpty())
        })
    }

    @Test
    fun deleteDrawing_success() = runTest {
        // Given
        val drawing = Drawing(1, "Test Drawing", "", "")

        // When
        viewModel.deleteDrawing(drawing)

        // Then
        advanceUntilIdle()
        verify(drawingDao, times(1)).deleteDrawing(drawing)
        verify(drawingDao, times(1)).getAllDrawings()
    }

    @Test
    fun loadDrawingById_notFound() = runTest {
        // Given
        val drawingId = 1
        whenever(drawingDao.getDrawingById(drawingId)).thenReturn(null)

        // When
        viewModel.loadDrawingById(drawingId)

        // Then
        advanceUntilIdle()
        verify(loadedPathsObserver, never()).onChanged(any())
    }

    @Test
    fun saveDrawing_emptyName() = runTest {
        // Given
        val drawingName = ""
        val paths = listOf<DrawingView.PathData>()

        // When
        viewModel.saveDrawing(drawingName, paths)

        // Then
        advanceUntilIdle()
        verify(drawingDao, never()).insertDrawing(any())
        verify(drawingDao, never()).getAllDrawings()
    }

    @Test
    fun loadAllDrawings_empty() = runTest {
        // Given
        whenever(drawingDao.getAllDrawings()).thenReturn(emptyList())

        // When
        viewModel.loadAllDrawings()

        // Then
        advanceUntilIdle()
        verify(allDrawingsObserver).onChanged(emptyList())
    }
}
