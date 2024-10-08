package com.example.drawingapp.data

import com.example.drawingapp.PenShape

data class SerializablePath(
    val points: List<Float>,
    val color: Int,
    val strokeWidth: Float,
    val alpha: Int,
    val shape: PenShape
)
