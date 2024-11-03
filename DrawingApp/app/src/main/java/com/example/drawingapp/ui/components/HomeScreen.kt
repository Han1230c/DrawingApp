package com.example.drawingapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.drawingapp.data.Drawing
import com.example.drawingapp.base64ToBitmap
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onStartDrawingClick: () -> Unit,
    drawings: List<Drawing>,
    sharedDrawings: List<Drawing>,
    onDrawingClick: (Drawing) -> Unit,
    onDeleteClick: (Drawing) -> Unit,
    onShareClick: (Drawing) -> Unit,
    onLogoutClick: () -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean = false
) {
    var showSplash by remember { mutableStateOf(true) }

    // Display splash screen for 2 seconds
    LaunchedEffect(key1 = true) {
        delay(2000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Drawing App") },
                    actions = {
                        IconButton(onClick = onLogoutClick) {
                            Icon(Icons.Default.ExitToApp, "Logout")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onStartDrawingClick) {
                    Icon(Icons.Default.Add, "New Drawing")
                }
            }
        ) { padding ->
            SwipeRefresh(
                state = rememberSwipeRefreshState(isLoading),
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section for user's drawings
                        item {
                            Text("My Drawings", style = MaterialTheme.typography.h5)
                        }

                        if (drawings.isEmpty()) {
                            item {
                                Text(
                                    "No drawings yet",
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        } else {
                            items(drawings) { drawing ->
                                DrawingListItem(
                                    drawing = drawing,
                                    onDrawingClick = onDrawingClick,
                                    onDeleteClick = onDeleteClick,
                                    onShareClick = onShareClick,
                                    isShared = false
                                )
                            }
                        }

                        // Section for shared drawings
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Shared Drawings", style = MaterialTheme.typography.h5)
                        }

                        if (sharedDrawings.isEmpty()) {
                            item {
                                Text(
                                    "No shared drawings available",
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        } else {
                            items(sharedDrawings) { drawing ->
                                DrawingListItem(
                                    drawing = drawing,
                                    onDrawingClick = onDrawingClick,
                                    onDeleteClick = null,
                                    onShareClick = null,
                                    isShared = true
                                )
                            }
                        }
                    }

                    // Show loading indicator at the center if data is loading
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(50.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawingListItem(
    drawing: Drawing,
    onDrawingClick: (Drawing) -> Unit,
    onDeleteClick: ((Drawing) -> Unit)?,
    onShareClick: ((Drawing) -> Unit)?,
    isShared: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDrawingClick(drawing) },
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = drawing.name,
                    style = MaterialTheme.typography.h6
                )

                if (drawing.thumbnail?.isNotEmpty() == true) {
                    val bitmap = remember(drawing.thumbnail) {
                        try {
                            base64ToBitmap(drawing.thumbnail)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Drawing thumbnail",
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
            }

            Row {
                if (!isShared && onShareClick != null) {
                    IconButton(onClick = { onShareClick(drawing) }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
                if (!isShared && onDeleteClick != null) {
                    IconButton(onClick = { onDeleteClick(drawing) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colors.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Welcome to Drawing App", style = MaterialTheme.typography.h4)
    }
}
