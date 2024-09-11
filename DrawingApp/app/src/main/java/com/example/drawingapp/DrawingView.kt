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

/**
 * Custom view class that handles drawing interactions.
 * It allows users to draw different shapes (round, square, star) on the canvas,
 * and updates the canvas according to touch events.
 */
class DrawingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // List of drawn paths and their associated paints
    private var paths = mutableListOf<Pair<Path, Paint>>()

    // The current path being drawn by the user
    private var currentPath = Path()

    // The paint configuration for the current path being drawn
    private var currentPaint = Paint()

    // The current shape to be drawn (round, square, star)
    private var currentShape = PenShape.ROUND

    // ViewModel that stores the drawing state and interacts with the view
    private lateinit var viewModel: DrawingViewModel

    /**
     * Method to set the ViewModel that the view will observe for changes.
     * This connects the ViewModel with the view to update the drawing state.
     */
    fun setViewModel(vm: DrawingViewModel) {
        viewModel = vm

        // Observe changes to the paths stored in the ViewModel
        viewModel.paths.observeForever { newPaths ->
            paths = newPaths
            invalidate() // Redraw the view when paths are updated
        }

        // Observe changes to the current paint style
        viewModel.currentPaint.observeForever { newPaint ->
            currentPaint = newPaint
        }

        // Observe changes to the current shape selected for drawing
        viewModel.currentShape.observeForever { newShape ->
            currentShape = newShape
        }
    }

    /**
     * Overrides the onDraw method to draw paths on the canvas.
     * It loops through the list of paths and paints them on the canvas.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw all previously drawn paths with their respective paints
        paths.forEach { (path, paint) ->
            canvas.drawPath(path, paint)
        }

        // Draw the current path as the user is actively drawing it
        canvas.drawPath(currentPath, currentPaint)
    }

    /**
     * Handles touch events to create paths based on user finger movements.
     * Depending on the selected shape, different paths (round, square, star) are drawn.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Capture the x and y coordinates of the touch event
        val x = event.x
        val y = event.y

        // Handle different touch events (ACTION_DOWN, ACTION_MOVE, ACTION_UP)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Start a new path when the user touches the screen
                currentPath = Path()

                // Move to the initial touch point depending on the selected shape
                when (currentShape) {
                    PenShape.ROUND, PenShape.SQUARE -> currentPath.moveTo(x, y)
                    PenShape.STAR -> drawStar(x, y) // Start drawing a star if the selected shape is STAR
                }
            }
            MotionEvent.ACTION_MOVE -> {
                // Continue drawing the path as the user moves their finger
                when (currentShape) {
                    PenShape.ROUND, PenShape.SQUARE -> currentPath.lineTo(x, y)
                    PenShape.STAR -> drawStar(x, y) // For a star, update its shape as the user moves
                }
            }
            MotionEvent.ACTION_UP -> {
                // Finalize the path once the user lifts their finger
                when (currentShape) {
                    PenShape.ROUND, PenShape.SQUARE -> currentPath.lineTo(x, y)
                    PenShape.STAR -> drawStar(x, y)
                }

                // Add the completed path to the ViewModel
                viewModel.addPath(currentPath)

                // Reset the current path for future drawing actions
                currentPath = Path()
            }
            else -> return false
        }

        // Invalidate the view to trigger a redraw after touch events
        invalidate()
        return true
    }

    /**
     * Method to draw a star shape based on the user's touch coordinates (x, y).
     * It calculates the points of a star using trigonometric functions and adds it to the path.
     */
    private fun drawStar(x: Float, y: Float) {
        // Define the outer and inner radius of the star based on the current stroke width
        val outerRadius = currentPaint.strokeWidth
        val innerRadius = outerRadius / 2

        // Create a new path for the star
        val path = Path()

        // Loop to calculate the 10 points of the star (5 outer and 5 inner)
        for (i in 0 until 10) {
            val angle = Math.PI * i / 5 // Divide the full circle (360 degrees) into 10 segments
            val radius = if (i % 2 == 0) outerRadius else innerRadius // Alternate between outer and inner points

            // Calculate the X and Y coordinates for each star point
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
