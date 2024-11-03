package com.example.drawingapp.viewmodel

import android.graphics.Path
import android.graphics.Paint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawingapp.data.Drawing
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.data.SerializablePath
import com.example.drawingapp.DrawingView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*

class DrawingViewModel(private val repository: DrawingRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _allDrawings = MutableLiveData<List<Drawing>>()
    val allDrawings: LiveData<List<Drawing>> = _allDrawings

    private val _loadedPaths = MutableLiveData<List<DrawingView.PathData>>()
    val loadedPaths: LiveData<List<DrawingView.PathData>> = _loadedPaths

    private val _sharedDrawings = MutableLiveData<List<Drawing>>()
    val sharedDrawings: LiveData<List<Drawing>> = _sharedDrawings

    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> = _errorEvent

    private var currentDrawingId: Int? = null

    // Job to handle automatic refresh
    private var autoRefreshJob: Job? = null

    var currentDrawingName: String = ""
        private set

    init {
        startAutoRefresh()
    }

    // Start automatic refresh to load shared drawings every 5 seconds
    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                loadSharedDrawings()
                delay(5000)
            }
        }
    }

    // Save a drawing with specified name and paths
    fun saveDrawing(name: String, paths: List<DrawingView.PathData>) {
        if (name.isBlank()) return

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val serializablePaths = paths.map { pathData ->
                    SerializablePath(
                        points = approximatePath(pathData.path),
                        color = pathData.paint.color,
                        strokeWidth = pathData.paint.strokeWidth,
                        alpha = pathData.paint.alpha,
                        shape = pathData.shape
                    )
                }

                val serializedPaths = Gson().toJson(serializablePaths)
                val thumbnail = ""

                if (currentDrawingId != null) {
                    val existingDrawing = repository.getDrawingById(currentDrawingId!!)
                    if (existingDrawing != null) {
                        val updatedDrawing = existingDrawing.copy(
                            name = name,
                            serializedPaths = serializedPaths,
                            thumbnail = thumbnail
                        )
                        repository.updateDrawing(updatedDrawing)
                        _errorEvent.value = "Drawing updated successfully"
                    }
                } else {
                    val drawing = Drawing(
                        name = name,
                        serializedPaths = serializedPaths,
                        thumbnail = thumbnail,
                        isShared = false
                    )
                    repository.insertDrawing(drawing)
                    _errorEvent.value = "Drawing saved successfully"
                }

                loadAllDrawings()
            } catch (e: Exception) {
                Log.e("DrawingViewModel", "Save failed: ${e.message}")
                _errorEvent.value = "Failed to save drawing: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Load shared drawings without showing a loading indicator (used for auto-refresh)
    fun loadSharedDrawings() {
        viewModelScope.launch {
            try {
                val drawings = repository.getSharedDrawings()
                _sharedDrawings.value = drawings
            } catch (e: Exception) {
                Log.e("DrawingViewModel", "Load shared drawings failed: ${e.message}")
                _errorEvent.value = "Failed to load shared drawings: ${e.message}"
                _sharedDrawings.value = emptyList()
            }
        }
    }

    // Share a drawing and refresh the lists
    fun shareDrawing(drawing: Drawing) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.shareDrawing(drawing)
                _errorEvent.value = "Drawing shared successfully"
                loadAllDrawings()
                loadSharedDrawings()
            } catch (e: Exception) {
                Log.e("DrawingViewModel", "Share failed: ${e.message}")
                _errorEvent.value = e.message ?: "Failed to share drawing"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Load all drawings for the current user
    fun loadAllDrawings() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val drawings = repository.getAllDrawings()
                _allDrawings.value = drawings
            } catch (e: Exception) {
                Log.e("DrawingViewModel", "Load all drawings failed: ${e.message}")
                _errorEvent.value = "Failed to load drawings: ${e.message}"
                _allDrawings.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Load a drawing by its ID and convert paths for display
    fun loadDrawingById(id: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val drawing = repository.getDrawingById(id)
                if (drawing != null) {
                    currentDrawingId = drawing.id
                    currentDrawingName = drawing.name
                    val paths = convertDrawingToPaths(drawing)
                    _loadedPaths.value = paths
                } else {
                    _errorEvent.value = "Drawing not found"
                }
            } catch (e: Exception) {
                Log.e("DrawingViewModel", "Load drawing failed: ${e.message}")
                _errorEvent.value = "Failed to load drawing: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Delete a drawing and refresh the list
    fun deleteDrawing(drawing: Drawing) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteDrawing(drawing)
                _errorEvent.value = "Drawing deleted successfully"
                loadAllDrawings()
            } catch (e: Exception) {
                Log.e("DrawingViewModel", "Delete failed: ${e.message}")
                _errorEvent.value = "Failed to delete drawing: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Approximate path coordinates from a Path object
    private fun approximatePath(path: Path): List<Float> {
        val points = mutableListOf<Float>()
        val pm = android.graphics.PathMeasure(path, false)
        do {
            val length = pm.length
            var distance = 0f
            val speed = 5f
            val position = FloatArray(2)
            while (distance < length) {
                pm.getPosTan(distance, position, null)
                points.add(position[0])
                points.add(position[1])
                distance += speed
            }
        } while (pm.nextContour())
        return points
    }

    // Convert a saved drawing into paths for display in the DrawingView
    private fun convertDrawingToPaths(drawing: Drawing): List<DrawingView.PathData> {
        val gson = Gson()
        val type = object : TypeToken<List<SerializablePath>>() {}.type
        val serializablePaths: List<SerializablePath> = gson.fromJson(drawing.serializedPaths, type)

        return serializablePaths.map { sp ->
            val path = Path()
            for (i in sp.points.indices step 2) {
                if (i == 0) {
                    path.moveTo(sp.points[i], sp.points[i + 1])
                } else {
                    path.lineTo(sp.points[i], sp.points[i + 1])
                }
            }
            val paint = Paint().apply {
                color = sp.color
                strokeWidth = sp.strokeWidth
                alpha = sp.alpha
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            DrawingView.PathData(path, paint, sp.shape)
        }
    }

    // Clear the current drawing's information
    fun clearCurrentDrawing() {
        currentDrawingId = null
        currentDrawingName = ""
        _loadedPaths.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
    }
}
