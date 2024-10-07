package com.example.drawingapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DrawingViewModelFactory(private val drawingDao: DrawingDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrawingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DrawingViewModel(drawingDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}