package com.example.drawingapp

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class HomeFragment : Fragment() {
    private val viewModel: DrawingViewModel by viewModels {
        DrawingViewModelFactory(AppDatabase.getDatabase(requireContext()).drawingDao())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
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

@Composable
fun HomeScreen(
    onStartDrawingClick: () -> Unit,
    drawings: List<Drawing>,
    onDrawingClick: (Drawing) -> Unit,
    onDeleteClick: (Drawing) -> Unit
) {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
    } else {
        // 原有的内容
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("My Drawings", style = MaterialTheme.typography.h4)

            Button(
                onClick = onStartDrawingClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Start New Drawing")
            }

            if (drawings.isEmpty()) {
                Text("No drawings found.", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Text("Saved Drawings:", style = MaterialTheme.typography.h6)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(drawings) { drawing ->
                        DrawingItem(drawing, onDrawingClick, onDeleteClick)
                    }
                }
            }
        }
    }
}



@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Welcome to Drawing App", style = MaterialTheme.typography.h4)
    }
}

@Composable
fun DrawingItem(
    drawing: Drawing,
    onClick: (Drawing) -> Unit,
    onDeleteClick: (Drawing) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(drawing) },
        elevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // 显示缩略图
            if (drawing.thumbnail.isNotEmpty()) {
                val bitmap = base64ToBitmap(drawing.thumbnail)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Thumbnail",
                    modifier = Modifier.size(50.dp)
                )
            }
            Text(
                text = drawing.name,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                style = MaterialTheme.typography.body1
            )
            IconButton(onClick = { onDeleteClick(drawing) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

fun base64ToBitmap(base64Str: String): android.graphics.Bitmap {
    val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}
