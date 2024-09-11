package com.example.drawingapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

/**
 * Enum class that defines the different shapes the user can draw: ROUND, SQUARE, STAR.
 */
enum class PenShape { ROUND, SQUARE, STAR }

/**
 * ViewModel class that stores and manages the drawing state, including paths,
 * paint properties (color, stroke width, etc.), and the selected shape for drawing.
 * The ViewModel helps retain data across configuration changes (e.g., screen rotation)
 * and provides lifecycle awareness for the drawing app.
 */
class DrawingViewModel : ViewModel() {

    // LiveData to store the list of drawn paths along with their associated paints
    private val _paths = MutableLiveData<MutableList<Pair<Path, Paint>>>(mutableListOf())

    // Public LiveData that exposes the list of paths for observation by other components
    val paths: LiveData<MutableList<Pair<Path, Paint>>> = _paths

    // LiveData to store the current paint configuration, including color, stroke width, etc.
    private val _currentPaint = MutableLiveData(Paint().apply {
        color = Color.BLACK // Set the default color to black
        style = Paint.Style.STROKE // Set to stroke mode (draw outlines only)
        strokeJoin = Paint.Join.ROUND // Use rounded joins between lines
        strokeCap = Paint.Cap.ROUND // Use rounded caps for the stroke
        strokeWidth = 8f // Set the default stroke width to 8 pixels
        isAntiAlias = true // Enable anti-aliasing for smoother edges
    })

    // Public LiveData that exposes the current paint configuration for observation
    val currentPaint: LiveData<Paint> = _currentPaint

    // LiveData to store the current drawing shape selected by the user (ROUND, SQUARE, STAR)
    private val _currentShape = MutableLiveData(PenShape.ROUND)

    // Public LiveData that exposes the current drawing shape for observation
    val currentShape: LiveData<PenShape> = _currentShape

    // LiveData to store the current alpha (opacity) value for the paint
    private val _currentAlpha = MutableLiveData(255)

    // Public LiveData that exposes the current alpha value for observation
    val currentAlpha: LiveData<Int> = _currentAlpha

    /**
     * Function to add a new path to the list of paths.
     * This stores the current path and its associated paint in the ViewModel.
     */
    fun addPath(path: Path) {
        // Retrieve the current list of paths or create a new list if null
        val currentPaths = _paths.value ?: mutableListOf()

        // Add the new path with a copy of the current paint settings
        currentPaths.add(Pair(path, Paint(_currentPaint.value)))

        // Update the LiveData with the new list of paths
        _paths.value = currentPaths
    }

    /**
     * Function to set the color of the current paint.
     * This allows the user to change the drawing color while maintaining the current alpha value.
     */
    fun setColor(color: Int) {
        // Update the color while retaining the current alpha value
        _currentPaint.value = _currentPaint.value?.apply {
            this.color = color
            alpha = _currentAlpha.value ?: 255 // Retain the existing alpha value
        }
    }

    /**
     * Function to set the stroke width of the current paint.
     * This allows the user to adjust the line thickness when drawing.
     */
    fun setStrokeWidth(width: Float) {
        // Update the stroke width in the current paint
        _currentPaint.value = _currentPaint.value?.apply { this.strokeWidth = width }
    }

    /**
     * Function to set the shape for drawing (ROUND, SQUARE, STAR).
     * This updates the current shape and modifies the paint properties accordingly.
     */
    fun setShape(shape: PenShape) {
        // Update the current shape LiveData
        _currentShape.value = shape
        _currentPaint.value = _currentPaint.value?.apply {
            when (shape) {
                PenShape.ROUND -> {
                    // Set rounded stroke properties for ROUND shape
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
                PenShape.SQUARE -> {
                    // Set square stroke properties for SQUARE shape
                    strokeCap = Paint.Cap.SQUARE
                    strokeJoin = Paint.Join.MITER
                }
                PenShape.STAR -> {
                    // No specific paint changes for STAR; shape is drawn in the custom view
                }
            }
        }
    }

    /**
     * Function to set the alpha (opacity) value for the current paint.
     * This adjusts the transparency of the drawing color.
     */
    fun setAlpha(alpha: Int) {
        // Update the current alpha value
        _currentAlpha.value = alpha
        // Ensure the paint color is updated with the new alpha value
        setColor(_currentPaint.value?.color ?: Color.BLACK)
    }

    /**
     * Function to clear all drawn paths from the canvas.
     * This effectively resets the drawing area by clearing the list of paths.
     */
    fun clearPaths() {
        // Reset the paths list
        _paths.value = mutableListOf()
    }
}
