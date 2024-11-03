package com.example

import com.example.config.DatabaseConfig
import com.example.exceptions.*
import com.example.exceptions.ErrorResponse
import com.example.exceptions.NotFoundException
import com.example.exceptions.ValidationException
import com.example.routes.drawingRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import org.slf4j.event.*
import org.slf4j.LoggerFactory

// Application configuration object
object ApplicationConfig {
    val isDevelopment = System.getenv("KTOR_ENV") != "production"
    val logger = LoggerFactory.getLogger("Application")
}

fun main() {
    try {
        embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
            // Initialize the database
            DatabaseConfig.init()
            ApplicationConfig.logger.info("Database initialized successfully")

            // Configure the application
            configureRouting()
            configureSerialization()
            configureMonitoring()
            configureErrorHandling()

            ApplicationConfig.logger.info("Server configured and starting...")
        }.start(wait = true)
    } catch (e: Exception) {
        ApplicationConfig.logger.error("Failed to start server", e)
        throw e
    }
}

fun Application.configureRouting() {
    routing {
        // Authentication middleware
        intercept(ApplicationCallPipeline.Call) {
            if (!call.request.path().startsWith("/public")) {
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw SecurityException("Missing or invalid authentication token")
                }
            }
        }
        drawingRoutes()
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
}

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val userAgent = call.request.headers["User-Agent"]
            "Status: $status, HTTP method: $httpMethod, User agent: $userAgent"
        }
    }
}

fun Application.configureErrorHandling() {
    val logger = LoggerFactory.getLogger("ErrorHandling")

    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            logger.warn("Validation error: ${cause.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    message = cause.message ?: "Validation failed",
                    code = "VALIDATION_ERROR"
                )
            )
        }

        exception<SecurityException> { call, cause ->
            logger.warn("Security error: ${cause.message}")
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(
                    message = cause.message ?: "Unauthorized access",
                    code = "UNAUTHORIZED"
                )
            )
        }

        exception<NotFoundException> { call, cause ->
            logger.info("Resource not found: ${cause.message}")
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    message = cause.message ?: "Resource not found",
                    code = "NOT_FOUND"
                )
            )
        }

        exception<ContentTransformationException> { call, cause ->
            logger.warn("Request body validation failed", cause)
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    message = "Invalid request format: ${cause.message}",
                    code = "INVALID_REQUEST"
                )
            )
        }

        exception<Throwable> { call, cause ->
            logger.error("Unhandled error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    message = "Internal server error",
                    code = "INTERNAL_ERROR",
                    details = if (ApplicationConfig.isDevelopment) cause.stackTraceToString() else null
                )
            )
        }
    }
}
