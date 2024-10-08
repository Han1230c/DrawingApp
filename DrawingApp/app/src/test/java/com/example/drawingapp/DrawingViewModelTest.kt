package com.example.drawingapp

import android.graphics.Paint
import android.graphics.Path
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.drawingapp.data.Drawing
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.viewmodel.DrawingViewModel
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

    private lateinit var repository: DrawingRepository
    private lateinit var allDrawingsObserver: Observer<List<Drawing>>
    private lateinit var loadedPathsObserver: Observer<List<DrawingView.PathData>>
    private lateinit var viewModel: DrawingViewModel

    @Before
    fun setup() {
        repository = mock()
        allDrawingsObserver = mock()
        loadedPathsObserver = mock()
        viewModel = DrawingViewModel(repository)
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
        val drawings = listOf(Drawing(1, "Test Drawing", "", ""))
        whenever(repository.getAllDrawings()).thenReturn(drawings)

        viewModel.loadAllDrawings()

        advanceUntilIdle()
        verify(repository, times(1)).getAllDrawings()
        verify(allDrawingsObserver).onChanged(drawings)
    }

    @Test
    fun saveDrawing_success() = runTest {
        val drawingName = "New Drawing"
        val paths = listOf(DrawingView.PathData(Path(), Paint(), PenShape.ROUND))

        viewModel.saveDrawing(drawingName, paths)

        advanceUntilIdle()
        verify(repository, times(1)).insertDrawing(any())
        verify(repository, times(1)).getAllDrawings()
    }

    @Test
    fun deleteDrawing_success() = runTest {
        val drawing = Drawing(1, "Test Drawing", "", "")

        viewModel.deleteDrawing(drawing)

        advanceUntilIdle()
        verify(repository, times(1)).deleteDrawing(drawing)
        verify(repository, times(1)).getAllDrawings()
    }

    @Test
    fun saveDrawing_emptyName() = runTest {
        val drawingName = ""
        val paths = listOf<DrawingView.PathData>()

        viewModel.saveDrawing(drawingName, paths)

        advanceUntilIdle()
        verify(repository, never()).insertDrawing(any())
        verify(repository, never()).getAllDrawings()
    }

    @Test
    fun loadDrawingById_success() = runTest {
        val drawingId = 1
        val drawing = Drawing(
            drawingId,
            "Test Drawing",
            "[{\"points\":[0,0,100,100],\"color\":0,\"strokeWidth\":5.0,\"alpha\":255,\"shape\":\"ROUND\"}]",
            ""
        )
        whenever(repository.getDrawingById(drawingId)).thenReturn(drawing)

        viewModel.loadDrawingById(drawingId)

        advanceUntilIdle()
        verify(loadedPathsObserver).onChanged(check { paths ->
            assertNotNull(paths)
            assertTrue(paths.isNotEmpty())
        })
    }

    @Test
    fun loadDrawingById_notFound() = runTest {
        val drawingId = 1
        whenever(repository.getDrawingById(drawingId)).thenReturn(null)

        viewModel.loadDrawingById(drawingId)

        advanceUntilIdle()
        verify(loadedPathsObserver, never()).onChanged(any())
    }

    @Test
    fun loadAllDrawings_empty() = runTest {
        whenever(repository.getAllDrawings()).thenReturn(emptyList())

        viewModel.loadAllDrawings()

        advanceUntilIdle()
        verify(allDrawingsObserver).onChanged(emptyList())
    }
}