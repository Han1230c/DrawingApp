package com.example.drawingapp.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.drawingapp.DrawingView
import com.example.drawingapp.PenShape
import com.example.drawingapp.R
import com.example.drawingapp.data.AppDatabase
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.viewmodel.DrawingViewModel
import com.example.drawingapp.viewmodel.DrawingViewModelFactory

class DrawingFragment : Fragment() {

    private val viewModel: DrawingViewModel by viewModels {
        val repository = DrawingRepository(AppDatabase.getDatabase(requireContext()).drawingDao())
        DrawingViewModelFactory(repository)
    }

    private lateinit var drawingView: DrawingView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_drawing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drawingView = view.findViewById(R.id.drawingView)

        val drawingId = arguments?.getInt("drawingId", -1) ?: -1
        if (drawingId != -1) {
            viewModel.loadDrawingById(drawingId)
        } else {
            viewModel.clearCurrentDrawing()
        }

        viewModel.loadedPaths.observe(viewLifecycleOwner) { paths ->
            paths?.let { drawingView.loadPaths(it) }
        }

        // Color buttons
        view.findViewById<Button>(R.id.buttonBlack).setOnClickListener {
            drawingView.setPaintColor(android.graphics.Color.BLACK)
        }
        view.findViewById<Button>(R.id.buttonRed).setOnClickListener {
            drawingView.setPaintColor(android.graphics.Color.RED)
        }
        view.findViewById<Button>(R.id.buttonBlue).setOnClickListener {
            drawingView.setPaintColor(android.graphics.Color.BLUE)
        }

        // Shape buttons
        view.findViewById<Button>(R.id.buttonRound).setOnClickListener {
            drawingView.setShape(PenShape.ROUND)
        }
        view.findViewById<Button>(R.id.buttonSquare).setOnClickListener {
            drawingView.setShape(PenShape.SQUARE)
        }
        view.findViewById<Button>(R.id.buttonStar).setOnClickListener {
            drawingView.setShape(PenShape.STAR)
        }

        // Stroke width
        val seekBarStrokeWidth = view.findViewById<SeekBar>(R.id.seekBarStrokeWidth)
        seekBarStrokeWidth.progress = 8
        seekBarStrokeWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                drawingView.setStrokeWidth(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Opacity
        val seekBarAlpha = view.findViewById<SeekBar>(R.id.seekBarAlpha)
        seekBarAlpha.progress = 255
        seekBarAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                drawingView.setAlpha(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Clear button
        view.findViewById<Button>(R.id.buttonClear).setOnClickListener {
            drawingView.clearCanvas()
        }

        // Save button
        view.findViewById<Button>(R.id.buttonSave).setOnClickListener {
            showSaveDialog()
        }

        // Back button
        view.findViewById<Button>(R.id.buttonBack).setOnClickListener {
            findNavController().navigate(R.id.action_drawingFragment_to_homeFragment)
        }
    }

    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val input = android.widget.EditText(requireContext())

        if (viewModel.currentDrawingName.isNotEmpty()) {
            builder.setTitle("Update Drawing")
            input.setText(viewModel.currentDrawingName)
        } else {
            builder.setTitle("Save New Drawing")
        }

        builder.setView(input)
        builder.setPositiveButton("Save") { dialog, _ ->
            val drawingName = input.text.toString()
            if (drawingName.isNotEmpty()) {
                val paths = drawingView.getPaths()
                viewModel.saveDrawing(drawingName, paths)
                Toast.makeText(
                    requireContext(),
                    if (viewModel.currentDrawingName.isNotEmpty()) "Drawing updated" else "New drawing saved",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_drawingFragment_to_homeFragment)
            } else {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}
