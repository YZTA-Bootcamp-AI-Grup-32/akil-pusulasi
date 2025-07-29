package com.example.akilpusulasi.network.network

import com.example.akilpusulasi.network.request.CreateUserRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("/api/v1/users/create") // Match the path in your FastAPI router
    suspend fun createUser(
        @Header("Authorization") token: String,
        @Body request: CreateUserRequest
    ): Response<Unit> // We don't expect a body back, just a success code (e.g. 201)

    // TODO: Add other endpoints here later (e.g., createGameSession)
}