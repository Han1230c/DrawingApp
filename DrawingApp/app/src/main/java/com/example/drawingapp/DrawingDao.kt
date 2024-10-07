package com.example.drawingapp

import androidx.room.*

@Dao
interface DrawingDao {
    @Query("SELECT * FROM drawings")
    suspend fun getAllDrawings(): List<Drawing>

    @Insert
    suspend fun insertDrawing(drawing: Drawing)

    @Delete
    suspend fun deleteDrawing(drawing: Drawing)

    @Query("SELECT * FROM drawings WHERE id = :id")
    suspend fun getDrawingById(id: Int): Drawing?
}