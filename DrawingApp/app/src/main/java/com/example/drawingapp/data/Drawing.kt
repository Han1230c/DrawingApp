package com.example.drawingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "drawings")
@TypeConverters(Converters::class)
data class Drawing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val serializedPaths: String,
    val thumbnail: String = "" // To store Base64 encoded thumbnail
)
