package com.example.drawingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "drawings")
@TypeConverters(Converters::class)
data class Drawing(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val serializedPaths: String,
    val thumbnail: String = "",  // Base64 encoded thumbnail
    val isShared: Boolean = false,  // New: indicates if the drawing is shared
    val userId: String? = null,     // New: user ID associated with the drawing
    val createdAt: Long = System.currentTimeMillis()  // New: timestamp for creation time
)
