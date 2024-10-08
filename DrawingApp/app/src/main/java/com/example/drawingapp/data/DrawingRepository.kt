package com.example.drawingapp.data

open class DrawingRepository(private val drawingDao: DrawingDao) {
    open suspend fun insertDrawing(drawing: Drawing) {
        drawingDao.insertDrawing(drawing)
    }

    open suspend fun deleteDrawing(drawing: Drawing) {
        drawingDao.deleteDrawing(drawing)
    }

    open suspend fun getAllDrawings(): List<Drawing> {
        return drawingDao.getAllDrawings()
    }

    open suspend fun getDrawingById(id: Int): Drawing? {
        return drawingDao.getDrawingById(id)
    }
}