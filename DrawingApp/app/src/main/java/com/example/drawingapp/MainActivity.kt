package com.example.drawingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProvider
import android.graphics.Color

// MainActivity serves as the entry point of the app and sets up the UI components and interactions
class MainActivity : AppCompatActivity() {

    // ViewModel to manage the drawing state and share it with the DrawingView
    private lateinit var viewModel: DrawingViewModel

    // The custom DrawingView where users will draw
    private lateinit var drawingView: DrawingView

    // onCreate() is called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Set the layout from XML

        // Initialize the ViewModel using the ViewModelProvider
        viewModel = ViewModelProvider(this).get(DrawingViewModel::class.java)

        // Find the custom DrawingView from the layout and set its ViewModel
        drawingView = findViewById(R.id.drawingView)
        drawingView.setViewModel(viewModel)

        // Set up the various UI controls (buttons, seek bars) to interact with the ViewModel
        setupColorButtons()
        setupStrokeWidthSeekBar()
        setupShapeButtons()
        setupAlphaSeekBar()
        setupClearButton()
    }

    // Sets up the color selection buttons, allowing users to choose between black, red, and blue
    private fun setupColorButtons() {
        // Find the button by its ID and set a click listener to change the drawing color to black
        findViewById<Button>(R.id.buttonBlack).setOnClickListener { viewModel.setColor(Color.BLACK) }

        // Set a click listener for the red button to change the color to red
        findViewById<Button>(R.id.buttonRed).setOnClickListener { viewModel.setColor(Color.RED) }

        // Set a click listener for the blue button to change the color to blue
        findViewById<Button>(R.id.buttonBlue).setOnClickListener { viewModel.setColor(Color.BLUE) }
    }

    // Sets up the SeekBar to allow users to adjust the stroke width for drawing
    private fun setupStrokeWidthSeekBar() {
        // Find the SeekBar by its ID and set a listener for when the user changes the SeekBar value
        findViewById<SeekBar>(R.id.seekBarStrokeWidth).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                // When the progress is changed, update the ViewModel with the new stroke width
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    viewModel.setStrokeWidth(progress.toFloat())
                }

                // Unused: Method triggered when the user starts touching the SeekBar
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                // Unused: Method triggered when the user stops touching the SeekBar
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }

    // Sets up the buttons that allow users to select different shapes (round, square, star)
    private fun setupShapeButtons() {
        // Button to switch to ROUND shape mode
        findViewById<Button>(R.id.buttonRound).setOnClickListener { viewModel.setShape(PenShape.ROUND) }

        // Button to switch to SQUARE shape mode
        findViewById<Button>(R.id.buttonSquare).setOnClickListener { viewModel.setShape(PenShape.SQUARE) }

        // Button to switch to STAR shape mode
        findViewById<Button>(R.id.buttonStar).setOnClickListener { viewModel.setShape(PenShape.STAR) }
    }

    // Sets up the SeekBar to adjust the alpha (opacity) of the drawing
    private fun setupAlphaSeekBar() {
        // Find the SeekBar by its ID and set a listener for when the alpha value is adjusted
        findViewById<SeekBar>(R.id.seekBarAlpha).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                // When the progress changes, update the ViewModel with the new alpha value
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    viewModel.setAlpha(progress) // Set alpha (opacity) based on SeekBar value (0-255)
                }

                // Unused: Method triggered when the user starts adjusting the SeekBar
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                // Unused: Method triggered when the user stops adjusting the SeekBar
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }

    // Sets up the clear button to allow the user to clear the canvas
    private fun setupClearButton() {
        // Find the button by its ID and set a click listener to clear all drawn paths
        findViewById<Button>(R.id.buttonClear).setOnClickListener { viewModel.clearPaths() }
    }
}
