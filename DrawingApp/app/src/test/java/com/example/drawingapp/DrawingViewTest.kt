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
        context = ApplicationProvider.getApplicationContext()
        drawingView = DrawingView(context)
    }

    @Test
    fun testInitialState() {
        val paths = drawingView.getPaths()
        assertTrue(paths.isEmpty())
    }

    @Test
    fun testDrawing() {
        simulateDrawing()
        val paths = drawingView.getPaths()
        assertEquals(1, paths.size)
    }

    @Test
    fun testClearCanvas() {
        simulateDrawing()
        drawingView.clearCanvas()
        val paths = drawingView.getPaths()
        assertTrue(paths.isEmpty())
    }

    @Test
    fun testSetPaintColor() {
        drawingView.setPaintColor(Color.RED)
        assertEquals(Color.RED, drawingView.getCurrentPaint().color)
    }

    @Test
    fun testSetStrokeWidth() {
        drawingView.setStrokeWidth(20f)
        assertEquals(20f, drawingView.getCurrentPaint().strokeWidth)
    }

    @Test
    fun testSetAlpha() {
        drawingView.setAlpha(128)
        assertEquals(128, drawingView.getCurrentPaint().alpha)
    }

    @Test
    fun testSetShape() {
        // Since shape affects drawing, we can only test if it doesn't crash
        drawingView.setShape(PenShape.SQUARE)
        simulateDrawing()
        // If we reach here without exception, the test passes
    }

    @Test
    fun testLoadPaths() {
        simulateDrawing()
        val originalPaths = drawingView.getPaths()

        val newDrawingView = DrawingView(context)
        newDrawingView.loadPaths(originalPaths)

        val loadedPaths = newDrawingView.getPaths()
        assertEquals(originalPaths.size, loadedPaths.size)
    }


    private fun simulateDrawing() {
        val downEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 10f, 10f, 0)
        val moveEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 20f, 20f, 0)
        val upEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 30f, 30f, 0)

        drawingView.onTouchEvent(downEvent)
        drawingView.onTouchEvent(moveEvent)
        drawingView.onTouchEvent(upEvent)

        downEvent.recycle()
        moveEvent.recycle()
        upEvent.recycle()
    }

    private fun drawToBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawingView.draw(canvas)
        return bitmap
    }
}