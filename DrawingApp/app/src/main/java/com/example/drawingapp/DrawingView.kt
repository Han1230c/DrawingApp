package com.example.drawingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

// Custom view for drawing
class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Path to store drawing
    private var path = Path()

    // Paint for drawing
    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 8f
        isAntiAlias = true
    }

    // Draw the path on the canvas
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    // Handle touch events for drawing
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.moveTo(x, y) // Start drawing
            MotionEvent.ACTION_MOVE -> path.lineTo(x, y) // Continue drawing
            MotionEvent.ACTION_UP -> {
                path.lineTo(x, y) // End drawing
            }
            else -> return false
        }

        // Redraw the view
        invalidate()
        return true
    }

    // Clear the canvas
    fun clearCanvas() {
        path.reset()
        invalidate()
    }
}