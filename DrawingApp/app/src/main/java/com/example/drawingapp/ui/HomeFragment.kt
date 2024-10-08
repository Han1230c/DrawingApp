package com.example.drawingapp.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.drawingapp.R
import com.example.drawingapp.data.AppDatabase
import com.example.drawingapp.data.Drawing
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.ui.components.HomeScreen
import com.example.drawingapp.viewmodel.DrawingViewModel
import com.example.drawingapp.viewmodel.DrawingViewModelFactory
import androidx.compose.runtime.livedata.observeAsState // Added import

class HomeFragment : Fragment() {
    private val viewModel: DrawingViewModel by viewModels {
        val repository = DrawingRepository(AppDatabase.getDatabase(requireContext()).drawingDao())
        DrawingViewModelFactory(repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    // Use observeAsState instead of collectAsState
                    val drawings by viewModel.allDrawings.observeAsState(initial = emptyList())
                    HomeScreen(
                        onStartDrawingClick = {
                            findNavController().navigate(R.id.action_homeFragment_to_drawingFragment)
                        },
                        drawings = drawings,
                        onDrawingClick = { drawing ->
                            val bundle = Bundle().apply {
                                putInt("drawingId", drawing.id)
                            }
                            findNavController().navigate(R.id.action_homeFragment_to_drawingFragment, bundle)
                        },
                        onDeleteClick = { drawing ->
                            showDeleteConfirmationDialog(drawing)
                        }
                    )
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(drawing: Drawing) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Drawing")
            .setMessage("Are you sure you want to delete \"${drawing.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteDrawing(drawing)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadAllDrawings()
    }
}
