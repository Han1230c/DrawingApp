package com.example.drawingapp.data

import android.util.Log
import com.example.drawingapp.api.ApiService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException

class DrawingRepository(private val drawingDao: DrawingDao) {
    private val auth = FirebaseAuth.getInstance()
    private val pendingUploads = mutableSetOf<Int>()
    private var lastSharedCount = 0
    private var lastLogTime = 0L

    companion object {
        private const val TAG = "DrawingRepository"
        private const val LOG_INTERVAL = 5000L
    }

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
    }

    // Function to insert a drawing into the local database and attempt to upload it to the server
    suspend fun insertDrawing(drawing: Drawing) {
        try {
            val userId = getCurrentUserId()
            val drawingWithUser = drawing.copy(
                userId = userId,
                createdAt = System.currentTimeMillis()
            )

            // Save to local database
            val savedDrawing = withContext(Dispatchers.IO) {
                val id = drawingDao.insertDrawing(drawingWithUser)
                drawingWithUser.copy(id = id.toInt())
            }

            // Attempt to upload to server
            try {
                val serverDrawing = uploadDrawingToServer(savedDrawing)
                // Update local ID to match server ID
                if (serverDrawing.id != savedDrawing.id) {
                    withContext(Dispatchers.IO) {
                        try {
                            drawingDao.safeUpdateDrawingId(savedDrawing.id, serverDrawing.id)
                            Log.d(TAG, "Successfully updated drawing ID from ${savedDrawing.id} to ${serverDrawing.id}")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to update drawing ID: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload drawing: ${e.message}")
                pendingUploads.add(savedDrawing.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save drawing: ${e.message}")
            throw Exception("Failed to save drawing: ${e.message}")
        }
    }

    // Function to upload a drawing to the server
    private suspend fun uploadDrawingToServer(drawing: Drawing): Drawing {
        try {
            val serverDrawing = ApiService.uploadDrawing(drawing)
            pendingUploads.remove(drawing.id)
            Log.d(TAG, "Successfully uploaded drawing ${drawing.id}")
            return serverDrawing
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}")
            pendingUploads.add(drawing.id)
            throw e
        }
    }

    // Function to synchronize pending drawings by uploading them to the server
    suspend fun syncPendingDrawings() {
        val pendingIds = pendingUploads.toSet() // Create a copy to avoid concurrent modification
        for (id in pendingIds) {
            try {
                val drawing = drawingDao.getDrawingById(id) ?: continue
                val serverDrawing = uploadDrawingToServer(drawing)
                // Update local ID to match server ID
                if (serverDrawing.id != drawing.id) {
                    withContext(Dispatchers.IO) {
                        try {
                            drawingDao.safeUpdateDrawingId(drawing.id, serverDrawing.id)
                            Log.d(TAG, "Successfully updated drawing ID from ${drawing.id} to ${serverDrawing.id}")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to update drawing ID: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync drawing $id: ${e.message}")
            }
        }
    }

    // Function to delete a drawing from the local database and the server
    suspend fun deleteDrawing(drawing: Drawing) {
        try {
            withContext(Dispatchers.IO) {
                drawingDao.deleteDrawing(drawing)
            }

            if (!pendingUploads.contains(drawing.id)) {
                try {
                    ApiService.deleteDrawing(drawing.id)
                    Log.d(TAG, "Successfully deleted drawing ${drawing.id}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete drawing from server: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete drawing: ${e.message}")
            throw Exception("Failed to delete drawing: ${e.message}")
        }
    }

    suspend fun deleteSharedDrawing(drawing: Drawing) {
        try {
            if (drawing.userId != getCurrentUserId()) {
                throw SecurityException("You can only delete your own shared drawings.")
            }

            ApiService.deleteDrawing(drawing.id)
            Log.d(TAG, "Successfully deleted shared drawing ${drawing.id}")

            withContext(Dispatchers.IO) {
                drawingDao.deleteDrawing(drawing)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete shared drawing: ${e.message}")
            throw e
        }
    }


    // Function to retrieve all drawings for the current user
    suspend fun getAllDrawings(): List<Drawing> {
        return try {
            withContext(Dispatchers.IO) {
                drawingDao.getAllDrawings(getCurrentUserId())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get drawings: ${e.message}")
            emptyList()
        }
    }

    // Function to retrieve all shared drawings from the server
    suspend fun getSharedDrawings(): List<Drawing> {
        return try {
            val drawings = ApiService.getSharedDrawings()
            val currentTime = System.currentTimeMillis()

            if (drawings.size != lastSharedCount || (currentTime - lastLogTime) > LOG_INTERVAL) {
                lastSharedCount = drawings.size
                lastLogTime = currentTime
                Log.d(TAG, "Got ${drawings.size} shared drawings from server")
            }
            drawings
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get shared drawings: ${e.message}")
            emptyList()
        }
    }

    suspend fun getDrawingById(id: Int): Drawing? {
        return try {
            withContext(Dispatchers.IO) {
                drawingDao.getDrawingById(id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get drawing by id: ${e.message}")
            null
        }
    }

    // Function to retrieve a drawing by its ID
    suspend fun getSharedDrawingById(id: Int): Drawing? {
        return try {
            ApiService.getSharedDrawingById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get shared drawing by id: ${e.message}")
            null
        }
    }


    // Function to update a drawing in the local database and on the server
    suspend fun updateDrawing(drawing: Drawing) {
        try {
            withContext(Dispatchers.IO) {
                drawingDao.updateDrawing(drawing)
            }

            if (!pendingUploads.contains(drawing.id)) {
                try {
                    val serverDrawing = uploadDrawingToServer(drawing)
                    // Update local ID to match server ID
                    if (serverDrawing.id != drawing.id) {
                        withContext(Dispatchers.IO) {
                            try {
                                drawingDao.safeUpdateDrawingId(drawing.id, serverDrawing.id)
                                Log.d(TAG, "Successfully updated drawing ID from ${drawing.id} to ${serverDrawing.id}")
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to update drawing ID: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload updated drawing: ${e.message}")
                    pendingUploads.add(drawing.id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update drawing: ${e.message}")
            throw Exception("Failed to update drawing: ${e.message}")
        }
    }

    // Function to share a drawing by updating its shared status locally and on the server
    suspend fun shareDrawing(drawing: Drawing) {
        try {
            if (drawing.userId != getCurrentUserId()) {
                throw IllegalStateException("Only the creator can share this drawing")
            }

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastLogTime > LOG_INTERVAL) {
                Log.d(TAG, "Starting to share drawing with id: ${drawing.id}")
                lastLogTime = currentTime
            }

            try {
                var serverDrawing = drawing
                // If in pending list or not on server, upload first
                if (pendingUploads.contains(drawing.id) || !ApiService.checkDrawingExists(drawing.id)) {
                    Log.d(TAG, "Uploading drawing before sharing...")
                    serverDrawing = uploadDrawingToServer(drawing)
                    // Update local ID to match server ID
                    if (serverDrawing.id != drawing.id) {
                        withContext(Dispatchers.IO) {
                            try {
                                drawingDao.safeUpdateDrawingId(drawing.id, serverDrawing.id)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to update drawing ID: ${e.message}")
                                serverDrawing = drawing  // Continue with original ID if update fails
                            }
                        }
                    }
                }

                // Update local status
                val updatedDrawing = serverDrawing.copy(isShared = true)
                withContext(Dispatchers.IO) {
                    drawingDao.updateDrawing(updatedDrawing)
                }

                // Attempt to share on server with retries
                retry(maxAttempts = 3) {
                    try {
                        ApiService.shareDrawing(serverDrawing.id)
                        Log.d(TAG, "Successfully shared drawing ${serverDrawing.id}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to share drawing on server: ${e.message}")
                        // Retry upload if 404 error occurs
                        if (e.message?.contains("404") == true) {
                            Log.d(TAG, "Drawing not found on server, trying to upload again")
                            serverDrawing = uploadDrawingToServer(updatedDrawing)
                            // Update local ID and retry share
                            if (serverDrawing.id != updatedDrawing.id) {
                                withContext(Dispatchers.IO) {
                                    try {
                                        drawingDao.safeUpdateDrawingId(updatedDrawing.id, serverDrawing.id)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Failed to update drawing ID: ${e.message}")
                                    }
                                }
                            }
                            ApiService.shareDrawing(serverDrawing.id)
                        } else {
                            throw e
                        }
                    }
                }
            } catch (e: Exception) {
                // Rollback local state if sharing fails
                try {
                    withContext(Dispatchers.IO) {
                        drawingDao.updateDrawing(drawing)
                    }
                } catch (rollbackError: Exception) {
                    Log.e(TAG, "Failed to rollback drawing state: ${rollbackError.message}")
                }
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share drawing: ${e.message}")
            throw e
        }
    }

    // Helper function to retry an operation with exponential backoff
    private suspend fun <T> retry(
        maxAttempts: Int,
        initialDelay: Long = 1000,
        maxDelay: Long = 5000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(maxAttempts - 1) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                Log.w(TAG, "Attempt ${attempt + 1} failed: ${e.message}")
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
        return block() // Final attempt
    }
}

