package com.example.repositories

import com.example.config.DatabaseConfig.dbQuery
import com.example.exceptions.DatabaseException  // Import for handling database errors
import com.example.exceptions.NotFoundException  // Import for handling not-found errors
import com.example.models.Drawing
import com.example.models.Drawings
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class DrawingRepository {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // Function to create a new drawing if it doesn't already exist
    suspend fun create(drawing: Drawing): Drawing = dbQuery {
        try {
            // Check if a similar drawing already exists based on specific conditions
            val existingDrawing = Drawings
                .select {
                    (Drawings.userId eq drawing.userId) and
                            (Drawings.imageData eq drawing.imageData) and
                            (Drawings.isShared eq drawing.isShared)
                }
                .map { toDrawing(it) }
                .firstOrNull()

            // Return the existing drawing if found
            if (existingDrawing != null) {
                logger.info("Drawing already exists with id: ${existingDrawing.id}")
                return@dbQuery existingDrawing
            }

            // Create a new drawing entry if not found
            val now = LocalDateTime.now()
            val id = Drawings.insert {
                it[userId] = drawing.userId
                it[imageData] = drawing.imageData
                it[title] = drawing.title
                it[isShared] = drawing.isShared
                it[createdAt] = now
                it[updatedAt] = now
            } get Drawings.id

            logger.info("Created drawing with id: $id for user: ${drawing.userId}")
            drawing.copy(id = id, createdAt = now, updatedAt = now)
        } catch (e: Exception) {
            logger.error("Failed to create drawing", e)
            throw DatabaseException("Failed to create drawing: ${e.message}")
        }
    }

    // Function to get a drawing by its ID
    suspend fun getById(id: Int): Drawing? = dbQuery {
        try {
            Drawings.select { Drawings.id eq id }
                .map { toDrawing(it) }
                .singleOrNull()
                ?.also { logger.info("Retrieved drawing with id: $id") }
                ?: throw NotFoundException("Drawing not found with ID: $id")
        } catch (e: Exception) {
            when (e) {
                is NotFoundException -> throw e
                else -> {
                    logger.error("Failed to get drawing by id: $id", e)
                    throw DatabaseException("Failed to fetch drawing: ${e.message}")
                }
            }
        }
    }

    // Function to retrieve all shared drawings
    suspend fun getAllSharedDrawings(): List<Drawing> = dbQuery {
        try {
            Drawings
                .select { Drawings.isShared eq true }
                .orderBy(Drawings.id to SortOrder.DESC)  // Order by ID in descending order to get the latest drawings
                .map { toDrawing(it) }
                .distinctBy { it.id }  // Ensure unique drawings by ID
                .also { logger.info("Retrieved ${it.size} shared drawings") }
        } catch (e: Exception) {
            logger.error("Failed to fetch shared drawings", e)
            throw DatabaseException("Failed to fetch shared drawings: ${e.message}")
        }
    }

    // Function to retrieve drawings by a specific user ID
    suspend fun getDrawingsByUserId(userId: String): List<Drawing> = dbQuery {
        try {
            Drawings.select { Drawings.userId eq userId }
                .map { toDrawing(it) }
                .also { logger.info("Retrieved ${it.size} drawings for user $userId") }
        } catch (e: Exception) {
            logger.error("Failed to fetch drawings for user: $userId", e)
            throw DatabaseException("Failed to fetch user drawings: ${e.message}")
        }
    }

    // Function to update the shared status of a drawing by its ID
    suspend fun updateShared(id: Int, shared: Boolean): Drawing = dbQuery {
        try {
            val now = LocalDateTime.now()

            // First, check if the drawing exists
            val drawing = getById(id) ?: throw NotFoundException("Drawing not found with id: $id")

            val updatedRows = Drawings.update({ Drawings.id eq id }) {
                it[isShared] = shared
                it[updatedAt] = now
            }

            if (updatedRows == 0) {
                throw DatabaseException("Failed to update drawing: no rows were affected")
            }

            logger.info("Updated drawing $id shared status to: $shared")
            drawing.copy(isShared = shared, updatedAt = now)
        } catch (e: Exception) {
            when (e) {
                is NotFoundException -> throw e
                else -> {
                    logger.error("Failed to update drawing shared status", e)
                    throw DatabaseException("Failed to update drawing: ${e.message}")
                }
            }
        }
    }

    // Function to delete a drawing by its ID
    suspend fun delete(id: Int, userId: String): Boolean = dbQuery {
        try {
            // Check if the drawing exists
            val drawing = getById(id) ?: throw NotFoundException("Drawing not found with ID: $id")

            // Check if the current user is the owner
            if (drawing.userId != userId) {
                throw SecurityException("You don't have permission to delete this drawing")
            }

            val deletedRows = Drawings.deleteWhere { Drawings.id eq id }

            if (deletedRows == 0) {
                throw DatabaseException("Failed to delete drawing: no rows were affected")
            }

            logger.info("Deleted drawing with id: $id")
            true
        } catch (e: Exception) {
            when (e) {
                is NotFoundException, is SecurityException -> throw e
                else -> {
                    logger.error("Failed to delete drawing", e)
                    throw DatabaseException("Failed to delete drawing: ${e.message}")
                }
            }
        }
    }

    // Helper function to map a database row to a Drawing object
    private fun toDrawing(row: ResultRow): Drawing =
        Drawing(
            id = row[Drawings.id],
            userId = row[Drawings.userId],
            imageData = row[Drawings.imageData],
            title = row[Drawings.title],
            createdAt = row[Drawings.createdAt],
            updatedAt = row[Drawings.updatedAt],
            isShared = row[Drawings.isShared]
        )
}
