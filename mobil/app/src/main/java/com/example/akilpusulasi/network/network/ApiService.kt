package com.example.akilpusulasi.network.network

import com.example.akilpusulasi.network.request.CreateUserRequest
import com.example.akilpusulasi.network.request.GameSessionCreateRequest
import com.example.akilpusulasi.network.response.GameParametersResponse
import com.example.akilpusulasi.network.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("/api/v1/users/create") // Match the path in your FastAPI router
    suspend fun createUser(
        @Header("Authorization") token: String,
        @Body request: CreateUserRequest
    ): Response<Unit> // We don't expect a body back, just a success code (e.g. 201)

    @POST("/api/v1/game-sessions") // Path from your backend router
    suspend fun createGameSession(
        @Header("Authorization") token: String,
        @Body request: GameSessionCreateRequest
    ): Response<Unit> // Backend returns a message, we just need the success code.

    @GET("/api/v1/new-session-parameters") // Path from your backend router
    suspend fun getNewGameParameters(
        @Header("Authorization") token: String
    ): Response<GameParametersResponse>

    @GET("/api/v1/users/me") // Path from your backend router
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<UserResponse> // We expect a UserResponse object on success
}