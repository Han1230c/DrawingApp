package com.example.drawingapp

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.runner.RunWith

@Config(manifest=Config.NONE, sdk = [28])
@RunWith(RobolectricTestRunner::class)
class DrawingViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: DrawingViewModel

    @Mock
    private lateinit var observer: Observer<MutableList<Pair<Path, Paint>>>

    /**
     * Setup method to initialize Mockito and the ViewModel before each test.
     */
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = DrawingViewModel()
    }

    /**
     * Test to verify the default values in the ViewModel.
     * The default color should be black, shape should be ROUND, alpha should be 255 (fully opaque),
     * and the default stroke width should be 8f.
     */
    @Test
    fun testDefaultValues() {
        assertEquals(Color.BLACK, viewModel.currentPaint.value?.color)
        assertEquals(PenShape.ROUND, viewModel.currentShape.value)
        assertEquals(255, viewModel.currentAlpha.value)
        assertEquals(8f, viewModel.currentPaint.value?.strokeWidth)
    }

    /**
     * Test to verify that the color can be set to different values.
     * This test sets the color to RED and BLUE, and checks if the color is updated correctly.
     */
    @Test
    fun testSetColor() {
        viewModel.setColor(Color.RED)
        assertEquals(Color.RED, viewModel.currentPaint.value?.color)

        viewModel.setColor(Color.BLUE)
        assertEquals(Color.BLUE, viewModel.currentPaint.value?.color)
    }

    /**
     * Test to verify that the stroke width can be set to different values.
     * This test sets the stroke width to 10f and 25f, and checks if the stroke width is updated correctly.
     */
    @Test
    fun testSetStrokeWidth() {
        viewModel.setStrokeWidth(10f)
        assertEquals(10f, viewModel.currentPaint.value?.strokeWidth)

        viewModel.setStrokeWidth(25f)
        assertEquals(25f, viewModel.currentPaint.value?.strokeWidth)
    }

    /**
     * Test to verify that the alpha (opacity) value can be set.
     * This test sets the alpha to 128 (50% opacity) and 255 (100% opacity), and checks if it is updated correctly.
     */
    @Test
    fun testSetAlpha() {
        viewModel.setAlpha(128)
        assertEquals(128, viewModel.currentAlpha.value)
        assertEquals(128, viewModel.currentPaint.value?.alpha)

        viewModel.setAlpha(255)
        assertEquals(255, viewModel.currentAlpha.value)
        assertEquals(255, viewModel.currentPaint.value?.alpha)
    }

    /**
     * Test to verify that the shape can be set to different values.
     * This test sets the shape to SQUARE and STAR, and checks if the shape is updated correctly.
     */
    @Test
    fun testSetShape() {
        viewModel.setShape(PenShape.SQUARE)
        assertEquals(PenShape.SQUARE, viewModel.currentShape.value)

        viewModel.setShape(PenShape.STAR)
        assertEquals(PenShape.STAR, viewModel.currentShape.value)
    }

    /**
     * Test to verify that a new path can be added to the ViewModel.
     * This test adds a path and checks if it is correctly added to the list of paths.
     */
    @Test
    fun testAddPath() {
        val path = Path()
        viewModel.addPath(path)

        assertEquals(1, viewModel.paths.value?.size)
        assertEquals(path, viewModel.paths.value?.get(0)?.first)
    }

    /**
     * Test to verify that all paths can be cleared from the ViewModel.
     * This test adds a path, clears all paths, and checks if the paths are removed.
     */
    @Test
    fun testClearPaths() {
        val path = Path()
        viewModel.addPath(path)
        assertEquals(1, viewModel.paths.value?.size)

        viewModel.clearPaths()
        assertEquals(0, viewModel.paths.value?.size)
    }

    /**
     * Test to verify the boundary conditions for stroke width.
     * This test sets the stroke width to 0f and 100f, and checks if the ViewModel handles it correctly.
     */
    @Test
    fun testStrokeWidthBoundary() {
        viewModel.setStrokeWidth(0f)
        assertEquals(0f, viewModel.currentPaint.value?.strokeWidth)

        viewModel.setStrokeWidth(100f)
        assertEquals(100f, viewModel.currentPaint.value?.strokeWidth)
    }

    /**
     * Test to verify the boundary conditions for alpha (opacity).
     * This test sets the alpha to 0 (completely transparent) and 255 (completely opaque),
     * and checks if the ViewModel handles it correctly.
     */
    @Test
    fun testAlphaBoundary() {
        viewModel.setAlpha(0)
        assertEquals(0, viewModel.currentAlpha.value)

        viewModel.setAlpha(255)
        assertEquals(255, viewModel.currentAlpha.value)
    }
}
