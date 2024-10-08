package com.example.drawingapp.data

import androidx.room.*

@Dao
interface DrawingDao {
    @Query("SELECT * FROM drawings ORDER BY id DESC")
    suspend fun getAllDrawings(): List<Drawing>

    @Insert
    suspend fun insertDrawing(drawing: Drawing)

    @Delete
    suspend fun deleteDrawing(drawing: Drawing)

    @Query("SELECT * FROM drawings WHERE id = :id")
    suspend fun getDrawingById(id: Int): Drawing?
}
