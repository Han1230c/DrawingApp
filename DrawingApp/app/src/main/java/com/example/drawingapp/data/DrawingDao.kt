package com.example.drawingapp.data

import androidx.room.*

@Dao
interface DrawingDao {
    @Query("SELECT * FROM drawings WHERE userId = :userId AND isShared = 0 ORDER BY id DESC")
    suspend fun getAllDrawings(userId: String): List<Drawing>

    @Query("SELECT * FROM drawings WHERE isShared = 1 AND userId != :userId ORDER BY id DESC")
    suspend fun getSharedDrawings(userId: String): List<Drawing>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: Drawing): Long

    @Delete
    suspend fun deleteDrawing(drawing: Drawing)

    @Query("SELECT * FROM drawings WHERE id = :id")
    suspend fun getDrawingById(id: Int): Drawing?

    @Update
    suspend fun updateDrawing(drawing: Drawing)

    // New safe method for updating the ID within a transaction
    @Transaction
    suspend fun safeUpdateDrawingId(oldId: Int, newId: Int) {
        // Retrieve the complete drawing object
        val drawing = getDrawingById(oldId) ?: return

        // Create a new drawing object with the updated ID
        val newDrawing = drawing.copy(id = newId)

        // Delete the old record
        deleteDrawing(drawing)

        // Insert the new record
        insertDrawing(newDrawing)
    }
}
