package com.example.drawingapp.viewmodel

import android.graphics.Path
import android.graphics.Paint
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
import kotlinx.coroutines.launch

class DrawingViewModel(private val repository: DrawingRepository) : ViewModel() {

    private val _allDrawings = MutableLiveData<List<Drawing>>()
    val allDrawings: LiveData<List<Drawing>> = _allDrawings

    private val _loadedPaths = MutableLiveData<List<DrawingView.PathData>>()
    val loadedPaths: LiveData<List<DrawingView.PathData>> = _loadedPaths

    // Remove init block to avoid automatic loading

    fun saveDrawing(name: String, paths: List<DrawingView.PathData>) {
        if (name.isBlank()) {
            return
        }
        viewModelScope.launch {
            try {
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

                // Generate thumbnail (Optional: Implement thumbnail generation logic)
                val thumbnail = "" // Placeholder

                val drawing = Drawing(
                    name = name,
                    serializedPaths = serializedPaths,
                    thumbnail = thumbnail
                )
                repository.insertDrawing(drawing)

                loadAllDrawings()
            } catch (e: Exception) {
                // Handle exception (e.g., log error)
            }
        }
    }

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

    fun loadAllDrawings() {
        viewModelScope.launch {
            val drawings = repository.getAllDrawings()
            _allDrawings.value = drawings
        }
    }

    fun loadDrawingById(id: Int) {
        viewModelScope.launch {
            try {
                val drawing = repository.getDrawingById(id)
                if (drawing != null) {
                    val paths = convertDrawingToPaths(drawing)
                    _loadedPaths.value = paths
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

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

    fun deleteDrawing(drawing: Drawing) {
        viewModelScope.launch {
            repository.deleteDrawing(drawing)
            loadAllDrawings()
        }
    }
}