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
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class DrawingFragment : Fragment() {

    private val viewModel: DrawingViewModel by viewModels {
        val repository = DrawingRepository(AppDatabase.getDatabase(requireContext()).drawingDao())
        DrawingViewModelFactory(repository)
    }

    private lateinit var drawingView: DrawingView
    private lateinit var loadingView: View // Loading view

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_drawing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize loading view
        loadingView = view.findViewById(R.id.loadingView)
        drawingView = view.findViewById(R.id.drawingView)

        // Observe loading status
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        viewModel.errorEvent.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        val drawingId = arguments?.getInt("drawingId", -1) ?: -1
        if (drawingId != -1) {
            viewModel.loadDrawingById(drawingId)
        } else {
            viewModel.clearCurrentDrawing()
        }

        viewModel.loadedPaths.observe(viewLifecycleOwner) { paths ->
            paths?.let { drawingView.loadPaths(it) }
        }

        // Color selection buttons
        view.findViewById<Button>(R.id.buttonBlack).setOnClickListener {
            drawingView.setPaintColor(android.graphics.Color.BLACK)
        }
        view.findViewById<Button>(R.id.buttonRed).setOnClickListener {
            drawingView.setPaintColor(android.graphics.Color.RED)
        }
        view.findViewById<Button>(R.id.buttonBlue).setOnClickListener {
            drawingView.setPaintColor(android.graphics.Color.BLUE)
        }

        // Shape selection buttons
        view.findViewById<Button>(R.id.buttonRound).setOnClickListener {
            drawingView.setShape(PenShape.ROUND)
        }
        view.findViewById<Button>(R.id.buttonSquare).setOnClickListener {
            drawingView.setShape(PenShape.SQUARE)
        }
        view.findViewById<Button>(R.id.buttonStar).setOnClickListener {
            drawingView.setShape(PenShape.STAR)
        }
        view.findViewById<Button>(R.id.buttonBall).setOnClickListener {
            drawingView.setBallMode(true)
        }

        // Stroke width adjustment
        val seekBarStrokeWidth = view.findViewById<SeekBar>(R.id.seekBarStrokeWidth)
        seekBarStrokeWidth.progress = 8
        seekBarStrokeWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    drawingView.setStrokeWidth(progress.toFloat())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        drawingView.setOnStrokeWidthChangedListener { width ->
            seekBarStrokeWidth.progress = width.toInt()
        }

        // Opacity adjustment
        val seekBarAlpha = view.findViewById<SeekBar>(R.id.seekBarAlpha)
        seekBarAlpha.progress = 255
        seekBarAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                drawingView.setAlpha(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Clear canvas button
        view.findViewById<Button>(R.id.buttonClear).setOnClickListener {
            drawingView.clearCanvas()
        }

        // Save drawing button
        view.findViewById<Button>(R.id.buttonSave).setOnClickListener {
            if (viewModel.isLoading.value == true) {
                Toast.makeText(requireContext(), "Please wait...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showSaveDialog()
        }

        // Navigate back button
        view.findViewById<Button>(R.id.buttonBack).setOnClickListener {
            findNavController().navigate(R.id.action_drawingFragment_to_homeFragment)
        }

        // Share drawing button
        view.findViewById<Button>(R.id.buttonShare).setOnClickListener {
            if (viewModel.isLoading.value == true) {
                Toast.makeText(requireContext(), "Please wait...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            shareDrawing()
        }
    }

    // Show a dialog for saving the drawing with a name
    private fun showSaveDialog() {
        if (viewModel.isLoading.value == true) {
            return
        }

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
                try {
                    val paths = drawingView.getPaths()
                    viewModel.saveDrawing(drawingName, paths)
                    dialog.dismiss()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error saving drawing: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // Share the drawing as an image
    private fun shareDrawing() {
        if (viewModel.isLoading.value == true) {
            return
        }

        try {
            val bitmap = drawingView.getBitmapFromView()
            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs()

            val imagePath = File(cachePath, "shared_image.jpg")
            val stream = FileOutputStream(imagePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.close()

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                imagePath
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/jpeg"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share your drawing"))
        } catch (e: Exception) {
            Log.e("DrawingFragment", "Error sharing drawing: ${e.message}")
            Toast.makeText(context, "Error sharing drawing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
