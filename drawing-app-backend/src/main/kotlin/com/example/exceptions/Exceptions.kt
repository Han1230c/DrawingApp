package com.example.exceptions

import kotlinx.serialization.Serializable

class ValidationException(message: String) : Exception(message)
class SecurityException(message: String) : Exception(message)
class NotFoundException(message: String) : Exception(message)
class DatabaseException(message: String) : Exception(message)

@Serializable
data class ErrorResponse(
    val message: String,
    val code: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String? = null
)