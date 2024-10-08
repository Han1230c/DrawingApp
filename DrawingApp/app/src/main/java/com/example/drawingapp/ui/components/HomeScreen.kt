package com.example.drawingapp.ui.components

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
import androidx.compose.ui.unit.dp
import com.example.drawingapp.data.Drawing
import com.example.drawingapp.base64ToBitmap
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onStartDrawingClick: () -> Unit,
    drawings: List<Drawing>,
    onDrawingClick: (Drawing) -> Unit,
    onDeleteClick: (Drawing) -> Unit
) {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
    } else {
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
                Text(
                    "No drawings found.",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
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
