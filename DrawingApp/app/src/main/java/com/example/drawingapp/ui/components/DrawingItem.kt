package com.example.drawingapp.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.drawingapp.data.Drawing

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
            // Display thumbnail if available
            if (drawing.thumbnail.isNotEmpty()) {
                val bitmap = base64ToBitmap(drawing.thumbnail)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Thumbnail",
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = drawing.name,
                modifier = Modifier
                    .weight(1f),
                style = MaterialTheme.typography.body1
            )
            IconButton(onClick = { onDeleteClick(drawing) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

// Utility function to convert Base64 string to Bitmap
fun base64ToBitmap(base64Str: String): android.graphics.Bitmap {
    val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}
