package com.example.routes

import com.example.exceptions.ValidationException
import com.example.exceptions.SecurityException
import com.example.exceptions.NotFoundException
import com.example.models.Drawing
import com.example.models.DrawingInput
import com.example.repositories.DrawingRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

fun Route.drawingRoutes() {
    val repository = DrawingRepository()
    val logger = LoggerFactory.getLogger("DrawingRoutes")

    // Input validation function
    fun validateDrawing(input: DrawingInput) {
        when {
            input.imageData.isNullOrBlank() ->
                throw ValidationException("Image data is required")
            input.title?.length ?: 0 > 255 ->
                throw ValidationException("Title cannot be longer than 255 characters")
        }
    }

    // Function to validate if the user has access to a specific resource
    fun validateUserId(currentUserId: String, resourceUserId: String) {
        if (currentUserId != resourceUserId) {
            throw SecurityException("You don't have permission to access this resource")
        }
    }

    // Routes that require validation
    route("/drawings") {
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw ValidationException("Invalid drawing ID format")

                val userId = call.request.headers["Authorization"]?.substringAfter("Bearer ")
                    ?: throw SecurityException("Missing authorization token")

                val drawing = repository.getById(id)
                    ?: throw NotFoundException("Drawing not found with ID: $id")

                if (!drawing.isShared && drawing.userId != userId) {
                    throw SecurityException("You don't have permission to view this drawing")
                }

                call.respond(drawing)
            } catch (e: Exception) {
                logger.error("Error getting drawing", e)
                throw e
            }
        }

        post {
            try {
                val userId = call.request.headers["Authorization"]?.substringAfter("Bearer ")
                    ?: throw SecurityException("Missing authorization token")

                val input = call.receive<DrawingInput>()
                validateDrawing(input)

                val drawing = Drawing(
                    userId = userId,
                    imageData = input.imageData!!,
                    title = input.title,
                    isShared = input.isShared
                )

                val savedDrawing = repository.create(drawing)
                call.respond(HttpStatusCode.Created, savedDrawing)
            } catch (e: Exception) {
                logger.error("Error creating drawing", e)
                throw e
            }
        }

        post("/{id}/share") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw ValidationException("Invalid drawing ID format")

                val userId = call.request.headers["Authorization"]?.substringAfter("Bearer ")
                    ?: throw SecurityException("Missing authorization token")

                val drawing = repository.getById(id)
                    ?: throw NotFoundException("Drawing not found with ID: $id")

                if (drawing.userId != userId) {
                    throw SecurityException("Only the owner can share this drawing")
                }

                val updatedDrawing = repository.updateShared(id, true)
                call.respond(updatedDrawing)
            } catch (e: Exception) {
                logger.error("Error sharing drawing", e)
                throw e
            }
        }
    }

    // Public routes
    route("/public") {
        get("/drawings/shared") {
            try {
                val drawings = repository.getAllSharedDrawings()
                call.respond(drawings)
            } catch (e: Exception) {
                logger.error("Error getting shared drawings", e)
                throw e
            }
        }
    }
}
