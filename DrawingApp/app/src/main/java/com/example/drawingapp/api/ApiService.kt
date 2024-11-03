package com.example.drawingapp.api

import android.util.Log
import com.example.drawingapp.MainActivity
import com.example.drawingapp.data.Drawing
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object ApiService {
    private const val BASE_URL = "http://${MainActivity.SERVER_IP}:${MainActivity.SERVER_PORT}"
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor())
        .addInterceptor(RetryInterceptor())
        .build()

    // Interceptor for authentication
    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val auth = FirebaseAuth.getInstance()
            val token = auth.currentUser?.uid
                ?: throw IOException("User not authenticated")

            val request = original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()

            return chain.proceed(request)
        }
    }

    // Interceptor to retry requests on failure
    private class RetryInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var attempts = 0
            var lastException: IOException? = null
            var lastResponse: Response? = null

            while (attempts < 3) {
                try {
                    // Close previous response
                    lastResponse?.close()

                    Log.d("ApiService", "Attempt ${attempts + 1} - URL: ${chain.request().url}")
                    val response = chain.proceed(chain.request())

                    if (response.isSuccessful) {
                        return response
                    }

                    Log.w("ApiService", "Request failed with code: ${response.code}")
                    if (response.code == 401) {
                        response.close()
                        throw IOException("Unauthorized")
                    }

                    lastResponse = response
                    attempts++

                    if (attempts < 3) {
                        val delay = 1000L * attempts
                        Log.d("ApiService", "Retrying after ${delay}ms")
                        Thread.sleep(delay)
                        continue
                    }

                    // Return last response after max attempts
                    return response
                } catch (e: IOException) {
                    Log.e("ApiService", "Request failed: ${e.message}")
                    lastException = e
                    attempts++

                    if (attempts < 3) {
                        val delay = 1000L * attempts
                        Log.d("ApiService", "Retrying after ${delay}ms")
                        Thread.sleep(delay)
                        continue
                    }
                }
            }

            lastResponse?.close()
            throw lastException ?: IOException("Request failed after 3 attempts")
        }
    }

    // Data transformation extension function
    private fun Drawing.toServerModel() = mapOf(
        "imageData" to serializedPaths,
        "title" to name,
        "isShared" to isShared,
        "userId" to (FirebaseAuth.getInstance().currentUser?.uid ?: "")
    )

    private fun Map<String, Any>.toDrawing() = Drawing(
        id = (this["id"] as? Number)?.toInt() ?: 0,
        name = this["title"] as? String ?: "",
        serializedPaths = this["imageData"] as? String ?: "",
        isShared = this["isShared"] as? Boolean ?: false,
        userId = this["userId"] as? String
    )

    // Function to check if a drawing exists
    suspend fun checkDrawingExists(id: Int) = suspendCoroutine<Boolean> { continuation ->
        val request = Request.Builder()
            .url("$BASE_URL/drawings/$id")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    when (response.code) {
                        200 -> continuation.resume(true)
                        404 -> continuation.resume(false)
                        else -> {
                            val errorBody = response.body?.string()
                            continuation.resumeWithException(
                                IOException("Unexpected response ${response.code}, body: $errorBody")
                            )
                        }
                    }
                }
            }
        })
    }

    // Function to upload a drawing
    suspend fun uploadDrawing(drawing: Drawing) = suspendCoroutine<Drawing> { continuation ->
        try {
            val json = gson.toJson(drawing.toServerModel())
            val requestBody = json.toRequestBody(JSON)
            val request = Request.Builder()
                .url("$BASE_URL/drawings")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("ApiService", "Upload failed: ${e.message}")
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (response.isSuccessful) {
                            response.body?.string()?.let { json ->
                                try {
                                    val type = object : TypeToken<Map<String, Any>>() {}.type
                                    val serverDrawing = gson.fromJson<Map<String, Any>>(json, type)
                                    continuation.resume(serverDrawing.toDrawing())
                                } catch (e: Exception) {
                                    continuation.resumeWithException(e)
                                }
                            } ?: continuation.resumeWithException(IOException("Empty response body"))
                        } else {
                            val errorBody = response.body?.string()
                            Log.e(
                                "ApiService", """
                            Upload failed:
                            Code: ${response.code}
                            Body: $errorBody
                            URL: ${call.request().url}
                            Method: ${call.request().method}
                        """.trimIndent()
                            )
                            continuation.resumeWithException(
                                IOException("Upload failed with code: ${response.code}, body: $errorBody")
                            )
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("ApiService", "Upload error: ${e.message}")
            continuation.resumeWithException(e)
        }
    }

    suspend fun getSharedDrawingById(id: Int) = suspendCoroutine<Drawing?> { continuation ->
        val request = Request.Builder()
            .url("$BASE_URL/public/drawings/shared/$id")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        response.body?.string()?.let { json ->
                            try {
                                val type = object : TypeToken<Map<String, Any>>() {}.type
                                val serverDrawing = gson.fromJson<Map<String, Any>>(json, type)
                                continuation.resume(serverDrawing.toDrawing())
                            } catch (e: Exception) {
                                continuation.resumeWithException(e)
                            }
                        } ?: continuation.resume(null)
                    } else {
                        val errorBody = response.body?.string()
                        continuation.resumeWithException(
                            IOException("Unexpected response ${response.code}, body: $errorBody")
                        )
                    }
                }
            }
        })
    }

    // Function to retrieve shared drawings
    suspend fun getSharedDrawings() = suspendCoroutine<List<Drawing>> { continuation ->
        val request = Request.Builder()
            .url("$BASE_URL/public/drawings/shared")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiService", "Failed to get shared drawings: ${e.message}")
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        response.body?.string()?.let { json ->
                            try {
                                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                                val serverDrawings =
                                    gson.fromJson<List<Map<String, Any>>>(json, type)
                                val drawings = serverDrawings.map { it.toDrawing() }
                                continuation.resume(drawings)
                            } catch (e: Exception) {
                                Log.e("ApiService", "Failed to parse drawings: ${e.message}")
                                continuation.resumeWithException(e)
                            }
                        } ?: continuation.resume(emptyList())
                    } else {
                        val errorBody = response.body?.string()
                        Log.e(
                            "ApiService", """
                            Get shared drawings failed:
                            Code: ${response.code}
                            Body: $errorBody
                            URL: ${call.request().url}
                            Method: ${call.request().method}
                        """.trimIndent()
                        )
                        continuation.resumeWithException(
                            IOException("Unexpected response ${response.code}, body: $errorBody")
                        )
                    }
                }
            }
        })
    }

    // Function to share a drawing
    suspend fun shareDrawing(id: Int) = suspendCoroutine<Boolean> { continuation ->
        try {
            val requestBody = gson.toJson(mapOf<String, Any>()).toRequestBody(JSON)
            val request = Request.Builder()
                .url("$BASE_URL/drawings/$id/share")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("ApiService", "Share failed: ${e.message}")
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { // Automatically close response after use
                        try {
                            if (response.isSuccessful) {
                                continuation.resume(true)
                            } else {
                                val errorBody = response.body?.string()
                                Log.e("ApiService", """
                                Share failed:
                                Code: ${response.code}
                                Body: $errorBody
                                URL: ${call.request().url}
                                Method: ${call.request().method}
                            """.trimIndent())
                                continuation.resumeWithException(
                                    IOException("Share failed with code: ${response.code}, body: $errorBody")
                                )
                            }
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("ApiService", "Share error: ${e            .message}")
            continuation.resumeWithException(e)
        }
    }

    // Function to delete a drawing
    suspend fun deleteDrawing(id: Int) = suspendCoroutine<Boolean> { continuation ->
        val request = Request.Builder()
            .url("$BASE_URL/drawings/$id")
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiService", "Delete failed: ${e.message}")
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        continuation.resume(true)
                    } else {
                        val errorBody = response.body?.string()
                        Log.e(
                            "ApiService", """
                        Delete failed:
                        Code: ${response.code}
                        Body: $errorBody
                        URL: ${call.request().url}
                        Method: ${call.request().method}
                    """.trimIndent()
                        )
                        if (response.code == 404) {
                            continuation.resume(true) // Treat as successful if the resource doesn't exist
                        } else {
                            continuation.resumeWithException(
                                IOException("Delete failed with code: ${response.code}, body: $errorBody")
                            )
                        }
                    }
                }
            }
        })
    }
}

