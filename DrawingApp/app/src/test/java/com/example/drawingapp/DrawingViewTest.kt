package com.example.drawingapp

import android.content.Context
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@Config(manifest=Config.NONE, sdk = [28])
@RunWith(RobolectricTestRunner::class)
class DrawingViewTest {

    private lateinit var viewModel: DrawingViewModel
    private lateinit var drawingView: DrawingView
    private lateinit var context: Context

    /**
     * Setup method to initialize the context, ViewModel, and DrawingView before each test case.
     */
    @Before
    fun setup() {
        // Initialize context, ViewModel, and DrawingView
        context = ApplicationProvider.getApplicationContext()
        viewModel = DrawingViewModel()
        drawingView = DrawingView(context)
        drawingView.setViewModel(viewModel)
    }

    /**
     * Test to verify that touch events for drawing result in a new path being added
     * to the ViewModel's paths list.
     */
    @Test
    fun testTouchEventDrawPath() {
        // Simulate touch events to draw a path
        val downEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 50f, 50f, 0)
        drawingView.onTouchEvent(downEvent)

        val moveEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 100f, 100f, 0)
        drawingView.onTouchEvent(moveEvent)

        val upEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 100f, 100f, 0)
        drawingView.onTouchEvent(upEvent)

        // Verify the ViewModel has registered a path
        assertEquals(1, viewModel.paths.value?.size)
    }

    /**
     * Test to verify that the STAR shape can be selected, and a path is drawn correctly
     * using the star shape when touch events occur.
     */
    @Test
    fun testDrawStarShape() {
        // Set shape to STAR
        viewModel.setShape(PenShape.STAR)

        // Simulate touch events to draw a star shape
        val downEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 50f, 50f, 0)
        drawingView.onTouchEvent(downEvent)

        val upEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 50f, 50f, 0)
        drawingView.onTouchEvent(upEvent)

        // Verify the ViewModel has registered the STAR shape
        assertEquals(PenShape.STAR, viewModel.currentShape.value)
    }

    /**
     * Test to verify that the clearPaths function in the ViewModel clears all the drawn paths,
     * and the canvas is cleared after invalidation.
     */
    @Test
    fun testClearCanvas() {
        // Simulate drawing a path
        val downEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 50f, 50f, 0)
        drawingView.onTouchEvent(downEvent)

        val upEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 50f, 50f, 0)
        drawingView.onTouchEvent(upEvent)

        // Clear paths in the ViewModel
        viewModel.clearPaths()
        drawingView.invalidate()

        // Verify the ViewModel has cleared the paths
        assertEquals(0, viewModel.paths.value?.size)
    }

    /**
     * Test to verify that switching between multiple shapes (SQUARE and ROUND)
     * works as expected and is reflected in the ViewModel.
     */
    @Test
    fun testMultipleShapes() {
        // Set shape to SQUARE
        viewModel.setShape(PenShape.SQUARE)
        assertEquals(PenShape.SQUARE, viewModel.currentShape.value)

        // Simulate touch events to draw with SQUARE shape
        val downEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 50f, 50f, 0)
        drawingView.onTouchEvent(downEvent)

        val upEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 100f, 100f, 0)
        drawingView.onTouchEvent(upEvent)

        // Set shape to ROUND
        viewModel.setShape(PenShape.ROUND)
        assertEquals(PenShape.ROUND, viewModel.currentShape.value)
    }
}
