package com.example.drawingapp

import android.graphics.Path
import android.graphics.Paint
import android.widget.Button
import androidx.test.core.app.ActivityScenario

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class DrawingFragmentTest {

    @Test
    fun testClearButton_clearsCanvas() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val fragment = DrawingFragment()
            activity.supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.nav_host_fragment,
                    fragment
                )
                .commitNow()

            val drawingView = fragment.view?.findViewById<DrawingView>(R.id.drawingView)
            if (drawingView != null) {
                drawingView.setPaintColor(android.graphics.Color.RED)
                drawingView.loadPaths(
                    listOf(
                        DrawingView.PathData(
                            Path().apply {
                                moveTo(0f, 0f)
                                lineTo(100f, 100f)
                            },
                            Paint().apply {
                                color = android.graphics.Color.RED
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
