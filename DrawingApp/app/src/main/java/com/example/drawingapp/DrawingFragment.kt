package com.example.drawingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.graphics.Color

/**
 * DrawingFragment contains the main drawing functionality of the app.
 * It manages the DrawingView and provides UI controls for drawing operations.
 */
class DrawingFragment : Fragment() {
    // Shared ViewModel to manage drawing state
    private val viewModel: DrawingViewModel by activityViewModels()

    // Custom view for drawing
    private lateinit var drawingView: DrawingView

    /**
     * Called to create the view hierarchy associated with the fragment.
     * This method inflates the layout for the DrawingFragment.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_drawing, container, false)
    }

    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored in to the view.
     * This method sets up the DrawingView and initializes all UI controls.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize DrawingView and connect it to the ViewModel
        drawingView = view.findViewById(R.id.drawingView)
        drawingView.setViewModel(viewModel)

        // Set up all UI controls
        setupColorButtons(view)
        setupStrokeWidthSeekBar(view)
        setupShapeButtons(view)
        setupAlphaSeekBar(view)
        setupClearButton(view)
    }

    /**
     * Sets up the color selection buttons.
     */
    private fun setupColorButtons(view: View) {
        view.findViewById<Button>(R.id.buttonBlack).setOnClickListener { viewModel.setColor(Color.BLACK) }
        view.findViewById<Button>(R.id.buttonRed).setOnClickListener { viewModel.setColor(Color.RED) }
        view.findViewById<Button>(R.id.buttonBlue).setOnClickListener { viewModel.setColor(Color.BLUE) }
    }

    /**
     * Sets up the SeekBar to adjust the stroke width.
     */
    private fun setupStrokeWidthSeekBar(view: View) {
        view.findViewById<SeekBar>(R.id.seekBarStrokeWidth).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    viewModel.setStrokeWidth(progress.toFloat())
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }

    /**
     * Sets up the shape selection buttons.
     */
    private fun setupShapeButtons(view: View) {
        view.findViewById<Button>(R.id.buttonRound).setOnClickListener { viewModel.setShape(PenShape.ROUND) }
        view.findViewById<Button>(R.id.buttonSquare).setOnClickListener { viewModel.setShape(PenShape.SQUARE) }
        view.findViewById<Button>(R.id.buttonStar).setOnClickListener { viewModel.setShape(PenShape.STAR) }
    }

    /**
     * Sets up the SeekBar to adjust the alpha (opacity) of the drawing.
     */
    private fun setupAlphaSeekBar(view: View) {
        view.findViewById<SeekBar>(R.id.seekBarAlpha).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    viewModel.setAlpha(progress)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }

    /**
     * Sets up the clear button to reset the canvas.
     */
    private fun setupClearButton(view: View) {
        view.findViewById<Button>(R.id.buttonClear).setOnClickListener { viewModel.clearPaths() }
    }
}