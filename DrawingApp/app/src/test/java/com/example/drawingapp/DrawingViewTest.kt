package com.example.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.MotionEvent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DrawingViewTest {

    private lateinit var drawingView: DrawingView
    private lateinit var context: Context

    @Before
    fun setup() {
        // Setup the context and DrawingView instance before each test
        context = ApplicationProvider.getApplicationContext()
        drawingView = DrawingView(context)
    }

    @Test
    fun testInitialState() {
        // Test that the initial state of DrawingView contains no paths
        val paths = drawingView.getPaths()
        assertTrue(paths.isEmpty())  // Ensure no paths are present initially
    }

    @Test
    fun testDrawing() {
        // Test that drawing on the view creates a new path
        simulateDrawing()  // Simulate a drawing action
        val paths = drawingView.getPaths()
        assertEquals(1, paths.size)  // Verify that one path has been added
    }

    @Test
    fun testClearCanvas() {
        // Test clearing the canvas removes all paths
        simulateDrawing()  // Simulate a drawing action
        drawingView.clearCanvas()  // Clear the canvas
        val paths = drawingView.getPaths()
        assertTrue(paths.isEmpty())  // Ensure all paths are cleared
    }

    @Test
    fun testSetPaintColor() {
        // Test setting the paint color
        drawingView.setPaintColor(Color.RED)  // Set the paint color to red
        assertEquals(Color.RED, drawingView.getCurrentPaint().color)  // Verify the color is set correctly
    }

    @Test
    fun testSetStrokeWidth() {
        // Test setting the stroke width
        drawingView.setStrokeWidth(20f)  // Set stroke width to 20
        assertEquals(20f, drawingView.getCurrentPaint().strokeWidth)  // Verify the stroke width is updated
    }

    @Test
    fun testSetAlpha() {
        // Test setting the paint alpha (transparency)
        drawingView.setAlpha(128)  // Set alpha value to 128
        assertEquals(128, drawingView.getCurrentPaint().alpha)  // Verify the alpha value is updated
    }

    @Test
    fun testSetShape() {
        // Test setting the shape of the drawing tool
        drawingView.setShape(PenShape.SQUARE)  // Set the shape to square
        simulateDrawing()  // Simulate drawing with the new shape
        // If no exception occurs, the test passes
    }

    @Test
    fun testLoadPaths() {
        // Test loading paths from a previous drawing
        simulateDrawing()  // Simulate a drawing action
        val originalPaths = drawingView.getPaths()

        // Create a new DrawingView and load the original paths into it
        val newDrawingView = DrawingView(context)
        newDrawingView.loadPaths(originalPaths)

        // Verify that the paths were loaded correctly
        val loadedPaths = newDrawingView.getPaths()
        assertEquals(originalPaths.size, loadedPaths.size)  // Ensure the number of paths matches
    }

    // Helper function to simulate a drawing action by sending touch events
    private fun simulateDrawing() {
        val downEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 10f, 10f, 0)
        val moveEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 20f, 20f, 0)
        val upEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 30f, 30f, 0)

        // Simulate the drawing process with touch events
        drawingView.onTouchEvent(downEvent)
        drawingView.onTouchEvent(moveEvent)
        drawingView.onTouchEvent(upEvent)

        downEvent.recycle()
        moveEvent.recycle()
        upEvent.recycle()
    }

    // Helper function to draw the view to a bitmap for testing rendering
    private fun drawToBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawingView.draw(canvas)  // Draw the view onto a bitmap
        return bitmap
    }
}
