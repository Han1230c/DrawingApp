package com.example.drawingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProvider
import android.graphics.Color

/**
 * MainActivity serves as the entry point of the app and sets up the user interface (UI) components.
 * It initializes the ViewModel and manages interactions between the user and the drawing canvas.
 */
class MainActivity : AppCompatActivity() {

    // ViewModel that manages the drawing state and shares it with the DrawingView
    private lateinit var viewModel: DrawingViewModel

    // Custom DrawingView where users can draw shapes and lines
    private lateinit var drawingView: DrawingView

    /**
     * onCreate() is called when the activity is created.
     * This method initializes the ViewModel, connects the DrawingView to the ViewModel,
     * and sets up various UI controls like buttons and seek bars.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Set the layout from XML

        // Initialize the ViewModel using the ViewModelProvider
        viewModel = ViewModelProvider(this).get(DrawingViewModel::class.java)

        // Find the DrawingView in the layout and set its ViewModel
        drawingView = findViewById(R.id.drawingView)
        drawingView.setViewModel(viewModel)

        // Set up UI controls for color, stroke width, shape selection, alpha (opacity), and clear button
        setupColorButtons()
        setupStrokeWidthSeekBar()
        setupShapeButtons()
        setupAlphaSeekBar()
        setupClearButton()
    }

    /**
     * Sets up the color selection buttons.
     * These buttons allow the user to choose between different colors for drawing (black, red, and blue).
     */
    private fun setupColorButtons() {
        // Set up a button to change the drawing color to black
        findViewById<Button>(R.id.buttonBlack).setOnClickListener { viewModel.setColor(Color.BLACK) }

        // Set up a button to change the drawing color to red
        findViewById<Button>(R.id.buttonRed).setOnClickListener { viewModel.setColor(Color.RED) }

        // Set up a button to change the drawing color to blue
        findViewById<Button>(R.id.buttonBlue).setOnClickListener { viewModel.setColor(Color.BLUE) }
    }

    /**
     * Sets up the SeekBar to adjust the stroke width (line thickness) for drawing.
     * The user can change the stroke width using this SeekBar, and the ViewModel will be updated accordingly.
     */
    private fun setupStrokeWidthSeekBar() {
        // Find the SeekBar and set a listener to track changes in the stroke width
        findViewById<SeekBar>(R.id.seekBarStrokeWidth).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                // When the SeekBar progress changes, update the ViewModel with the new stroke width
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    viewModel.setStrokeWidth(progress.toFloat())
                }

                // Unused: Triggered when the user starts touching the SeekBar
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                // Unused: Triggered when the user stops touching the SeekBar
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }

    /**
     * Sets up the shape selection buttons.
     * These buttons allow the user to choose between different shapes (round, square, and star).
     * The selected shape is sent to the ViewModel.
     */
    private fun setupShapeButtons() {
        // Button to select the ROUND shape
        findViewById<Button>(R.id.buttonRound).setOnClickListener { viewModel.setShape(PenShape.ROUND) }

        // Button to select the SQUARE shape
        findViewById<Button>(R.id.buttonSquare).setOnClickListener { viewModel.setShape(PenShape.SQUARE) }

        // Button to select the STAR shape
        findViewById<Button>(R.id.buttonStar).setOnClickListener { viewModel.setShape(PenShape.STAR) }
    }

    /**
     * Sets up the SeekBar to adjust the alpha (opacity) of the drawing.
     * The user can change the transparency of the lines using this SeekBar, and the ViewModel will be updated accordingly.
     */
    private fun setupAlphaSeekBar() {
        // Find the SeekBar and set a listener to track changes in alpha value (opacity)
        findViewById<SeekBar>(R.id.seekBarAlpha).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                // When the progress changes, update the ViewModel with the new alpha value
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    viewModel.setAlpha(progress) // Update the alpha value (0-255)
                }

                // Triggered when the user starts touching the SeekBar
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                // Triggered when the user stops touching the SeekBar
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }

    /**
     * Sets up the clear button.
     * This button allows the user to clear all drawings on the canvas by resetting the paths in the ViewModel.
     */
    private fun setupClearButton() {
        // Set up the clear button to reset the canvas by clearing all paths in the ViewModel
        findViewById<Button>(R.id.buttonClear).setOnClickListener { viewModel.clearPaths() }
    }
}
