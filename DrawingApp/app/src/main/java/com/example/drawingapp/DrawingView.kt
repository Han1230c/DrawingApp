package com.example.drawingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

// Custom view class for handling drawing interactions
class DrawingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // List of paths and their corresponding paints that have been drawn on the canvas
    private var paths = mutableListOf<Pair<Path, Paint>>()

    // Current path being drawn
    private var currentPath = Path()

    // Current paint used for drawing the current path
    private var currentPaint = Paint()

    // Enum to handle different drawing shapes (like round, square, star)
    private var currentShape = PenShape.ROUND

    // ViewModel to observe the drawing state (e.g., paths, paint, shape) and update the view accordingly
    private lateinit var viewModel: DrawingViewModel

    // Method to set the ViewModel that the view will observe for changes
    fun setViewModel(vm: DrawingViewModel) {
        viewModel = vm

        // Observing changes to the paths list in the ViewModel
        viewModel.paths.observeForever { newPaths ->
            paths = newPaths
            invalidate() // Redraw the view whenever paths are updated
        }

        // Observing changes to the current paint style
        viewModel.currentPaint.observeForever { newPaint ->
            currentPaint = newPaint
        }

        // Observing changes to the current shape selected for drawing
        viewModel.currentShape.observeForever { newShape ->
            currentShape = newShape
        }
    }

    // Overriding the onDraw method to draw the paths on the canvas
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Loop through the list of paths and paints, and draw each path on the canvas
        paths.forEach { (path, paint) ->
            canvas.drawPath(path, paint)
        }

        // Draw the current path as it is being drawn by the user
        canvas.drawPath(currentPath, currentPaint)
    }

    // Handling touch events from the user to draw shapes based on their finger movement
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Get the current X and Y coordinates of the touch event
        val x = event.x
        val y = event.y

        // Switch case to handle different types of touch actions (e.g., finger down, finger move, finger up)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Start a new path when the user touches the screen
                currentPath = Path()

                // Move to the starting point based on the selected shape (round, square, star)
                when (currentShape) {
                    PenShape.ROUND, PenShape.SQUARE -> currentPath.moveTo(x, y)
                    PenShape.STAR -> drawStar(x, y) // Start drawing a star if the shape is set to star
                }
            }
            MotionEvent.ACTION_MOVE -> {
                // Continue drawing the path as the user moves their finger across the screen
                when (currentShape) {
                    PenShape.ROUND, PenShape.SQUARE -> currentPath.lineTo(x, y)
                    PenShape.STAR -> drawStar(x, y)
                }
            }
            MotionEvent.ACTION_UP -> {
                // Finalize the path once the user lifts their finger off the screen
                when (currentShape) {
                    PenShape.ROUND, PenShape.SQUARE -> currentPath.lineTo(x, y)
                    PenShape.STAR -> drawStar(x, y)
                }

                // Add the completed path to the ViewModel for persistence
                viewModel.addPath(currentPath)

                // Reset the current path for the next drawing action
                currentPath = Path()
            }
            else -> return false
        }

        // Invalidate the view to trigger a redraw after touch events
        invalidate()
        return true
    }

    // Method to draw a star shape based on touch coordinates (x, y)
    private fun drawStar(x: Float, y: Float) {
        // Define the outer and inner radius of the star based on the current paint's stroke width
        val outerRadius = currentPaint.strokeWidth
        val innerRadius = outerRadius / 2

        // Create a new path for the star
        val path = Path()

        // Loop to calculate the 10 points (5 outer, 5 inner) of the star
        for (i in 0 until 10) {
            val angle = Math.PI * i / 5 // Divide 360 degrees into 10 angles (for a 5-pointed star)
            val radius = if (i % 2 == 0) outerRadius else innerRadius // Alternate between outer and inner radius

            // Calculate the X and Y coordinates for each point of the star
            val pointX = x + (radius * sin(angle)).toFloat()
            val pointY = y - (radius * cos(angle)).toFloat()

            // Move to the first point or draw a line to the next point
            if (i == 0) path.moveTo(pointX, pointY) else path.lineTo(pointX, pointY)
        }

        // Close the path to complete the star shape
        path.close()

        // Add the star path to the current path being drawn
        currentPath.addPath(path)
    }
}
