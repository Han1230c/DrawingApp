package com.example.drawingapp

import android.graphics.Path
import android.graphics.Paint
import android.widget.Button
import androidx.test.core.app.ActivityScenario
import android.graphics.Color
import android.view.MotionEvent
import android.widget.SeekBar
import org.junit.Assert.*

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class DrawingFragmentTest {

    // Test for changing paint color to black
    @Test
    fun testChangePaintColor_black() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val fragment = DrawingFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commitNow()

            val drawingView = fragment.view?.findViewById<DrawingView>(R.id.drawingView)
            val blackButton = fragment.view?.findViewById<Button>(R.id.buttonBlack)

            blackButton?.performClick()

            assertNotNull(drawingView)
            assertEquals(Color.BLACK, drawingView?.getCurrentPaint()?.color)
        }
    }

    // Test for changing paint color to red
    @Test
    fun testChangePaintColor_red() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val fragment = DrawingFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commitNow()

            val drawingView = fragment.view?.findViewById<DrawingView>(R.id.drawingView)
            val redButton = fragment.view?.findViewById<Button>(R.id.buttonRed)

            redButton?.performClick()

            assertNotNull(drawingView)
            assertEquals(Color.RED, drawingView?.getCurrentPaint()?.color)
        }
    }

    // Test for changing paint color to blue
    @Test
    fun testChangePaintColor_blue() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val fragment = DrawingFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commitNow()

            val drawingView = fragment.view?.findViewById<DrawingView>(R.id.drawingView)
            val blueButton = fragment.view?.findViewById<Button>(R.id.buttonBlue)

            blueButton?.performClick()

            assertNotNull(drawingView)
            assertEquals(Color.BLUE, drawingView?.getCurrentPaint()?.color)
        }
    }

    // Test for changing shape to round
    @Test
    fun testChangeShape_round() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val fragment = DrawingFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commitNow()

            val drawingView = fragment.view?.findViewById<DrawingView>(R.id.drawingView)
            val roundButton = fragment.view?.findViewById<Button>(R.id.buttonRound)

            roundButton?.performClick()

            assertNotNull(drawingView)
            assertEquals(PenShape.ROUND, drawingView?.currentShape)
        }
    }

    // Test for changing shape to square
    @Test
    fun testChangeShape_square() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val fragment = DrawingFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commitNow()

            val drawingView = fragment.view?.findViewById<DrawingView>(R.id.drawingView)
            val squareButton = fragment.view?.findViewById<Button>(R.id.buttonSquare)

            squareButton?.performClick()

            assertNotNull(drawingView)
            assertEquals(PenShape.SQUARE, drawingView?.currentShape)
        }
    }

    // Test for changing shape to star
    @Test
    fun testChangeShape_star() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val fragment = DrawingFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commitNow()

            val drawingView = fragment.view?.findViewById<DrawingView>(R.id.drawingView)
            val starButton = fragment.view?.findViewById<Button>(R.id.buttonStar)

            starButton?.performClick()

            assertNotNull(drawingView)
            assertEquals(PenShape.STAR, drawingView?.currentShape)
        }
    }

    // Test for adjusting stroke width
    @Test
    fun testAdjustStrokeWidth() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val fragment = DrawingFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commitNow()

            val drawingView = fragment.view?.findViewById<DrawingView>(R.id.drawingView)
            val seekBarStrokeWidth = fragment.view?.findViewById<SeekBar>(R.id.seekBarStrokeWidth)

            seekBarStrokeWidth?.progress = 20

            val changeEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 0f, 0f, 0)
            seekBarStrokeWidth?.onTouchEvent(changeEvent)
            changeEvent.recycle()

            assertNotNull(drawingView)
            assertEquals(20f, drawingView?.getCurrentPaint()?.strokeWidth)
        }
    }

    // Test for adjusting alpha (transparency)
    @Test
    fun testAdjustAlpha() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val fragment = DrawingFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commitNow()

            val drawingView = fragment.view?.findViewById<DrawingView>(R.id.drawingView)
            val seekBarAlpha = fragment.view?.findViewById<SeekBar>(R.id.seekBarAlpha)

            seekBarAlpha?.progress = 128

            val changeEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 0f, 0f, 0)
            seekBarAlpha?.onTouchEvent(changeEvent)
            changeEvent.recycle()

            assertNotNull(drawingView)
            assertEquals(128, drawingView?.getCurrentPaint()?.alpha)
        }
    }

    // Test for drawing on canvas
    @Test
    fun testDrawingOnCanvas() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val fragment = DrawingFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commitNow()

            val drawingView = fragment.view?.findViewById<DrawingView>(R.id.drawingView)

            assertNotNull(drawingView)

            val downEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 50f, 50f, 0)
            val moveEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 100f, 100f, 0)
            val upEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 150f, 150f, 0)

            drawingView?.onTouchEvent(downEvent)
            drawingView?.onTouchEvent(moveEvent)
            drawingView?.onTouchEvent(upEvent)

            downEvent.recycle()
            moveEvent.recycle()
            upEvent.recycle()

            val paths = drawingView?.getPaths()
            assertNotNull(paths)
            assertTrue(paths!!.isNotEmpty())
            assertEquals(1, paths.size)
        }
    }

    // Test for clearing the canvas using the clear button
    @Test
    fun testClearButton_clearsCanvas() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val fragment = DrawingFragment()
            activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commitNow()

            val drawingView = fragment.view?.findViewById<DrawingView>(R.id.drawingView)
            if (drawingView != null) {
                drawingView.setPaintColor(Color.RED)
                drawingView.loadPaths(
                    listOf(
                        DrawingView.PathData(
                            Path().apply {
                                moveTo(0f, 0f)
                                lineTo(100f, 100f)
                            },
                            Paint().apply {
                                color = Color.RED
                                style = Paint.Style.STROKE
                                strokeWidth = 8f
                            },
                            PenShape.ROUND
                        )
                    )
                )

                val clearButton = fragment.view?.findViewById<Button>(R.id.buttonClear)
                clearButton?.performClick()

                val paths = drawingView.getPaths()
                assertTrue(paths.isEmpty())
            }
        }
    }
}
