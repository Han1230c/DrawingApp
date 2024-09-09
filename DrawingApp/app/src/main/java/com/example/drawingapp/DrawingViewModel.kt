package com.example.drawingapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

// Enum class to define the different shapes that the user can draw
enum class PenShape { ROUND, SQUARE, STAR }

// ViewModel class to store and manage the drawing state (paths, paint properties, shapes, etc.)
class DrawingViewModel : ViewModel() {

    // LiveData to track the list of drawn paths with their corresponding paints
    private val _paths = MutableLiveData<MutableList<Pair<Path, Paint>>>(mutableListOf())

    // Public LiveData that other components can observe for changes to the paths
    val paths: LiveData<MutableList<Pair<Path, Paint>>> = _paths

    // LiveData to store the current paint configuration (color, stroke width, etc.)
    private val _currentPaint = MutableLiveData(Paint().apply {
        color = Color.BLACK // Default color is set to black
        style = Paint.Style.STROKE // Set to stroke style (only outlines, no fill)
        strokeJoin = Paint.Join.ROUND // Rounded joins between lines
        strokeCap = Paint.Cap.ROUND // Rounded ends for the stroke
        strokeWidth = 8f // Default stroke width
        isAntiAlias = true // Anti-aliasing to smooth out edges
    })

    // Public LiveData to expose the current paint configuration
    val currentPaint: LiveData<Paint> = _currentPaint

    // LiveData to track the current shape selected by the user (round, square, star)
    private val _currentShape = MutableLiveData(PenShape.ROUND)

    // Public LiveData to expose the current shape selected
    val currentShape: LiveData<PenShape> = _currentShape

    // LiveData to store the current alpha (opacity) value for the paint
    private val _currentAlpha = MutableLiveData(255)

    // Public LiveData to expose the current alpha value
    val currentAlpha: LiveData<Int> = _currentAlpha

    // Function to add a new path to the list of paths
    fun addPath(path: Path) {
        // Retrieve the current list of paths, or create an empty list if null
        val currentPaths = _paths.value ?: mutableListOf()

        // Add the new path and the current paint as a pair to the list
        currentPaths.add(Pair(path, Paint(_currentPaint.value)))

        // Update the LiveData with the new list of paths
        _paths.value = currentPaths
    }

    // Function to set the color of the current paint
    fun setColor(color: Int) {
        // Update the color and maintain the current alpha (opacity) value
        _currentPaint.value = _currentPaint.value?.apply {
            this.color = color
            alpha = _currentAlpha.value ?: 255 // Ensure alpha is retained
        }
    }

    // Function to set the stroke width for the current paint
    fun setStrokeWidth(width: Float) {
        // Update the stroke width
        _currentPaint.value = _currentPaint.value?.apply { this.strokeWidth = width }
    }

    // Function to set the shape type (round, square, star) and adjust paint properties accordingly
    fun setShape(shape: PenShape) {
        _currentShape.value = shape // Update the current shape LiveData
        _currentPaint.value = _currentPaint.value?.apply {
            when (shape) {
                PenShape.ROUND -> {
                    // Set rounded stroke properties for the round shape
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
                PenShape.SQUARE -> {
                    // Set square stroke properties for the square shape
                    strokeCap = Paint.Cap.SQUARE
                    strokeJoin = Paint.Join.MITER
                }
                PenShape.STAR -> {
                    // No specific paint changes for star; handled in the DrawingView
                }
            }
        }
    }

    // Function to set the alpha (opacity) of the current paint
    fun setAlpha(alpha: Int) {
        _currentAlpha.value = alpha // Update the current alpha LiveData
        // Ensure the paint color is updated with the new alpha value
        setColor(_currentPaint.value?.color ?: Color.BLACK)
    }

    // Function to clear all drawn paths, resetting the drawing canvas
    fun clearPaths() {
        _paths.value = mutableListOf() // Reset the paths list
    }
}
