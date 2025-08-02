package com.example.akilpusulasi.network.network

import com.example.akilpusulasi.network.request.CreateUserRequest
import com.example.akilpusulasi.network.request.GameSessionCreateRequest
import com.example.akilpusulasi.network.response.GameParametersResponse
import com.example.akilpusulasi.network.response.UserResponse
import com.example.akilpusulasi.network.request.DailyJournalCreateRequest
import com.example.akilpusulasi.network.response.DailyJournalResponse
import com.example.akilpusulasi.network.response.UserStatsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("/api/v1/users/create")
    suspend fun createUser(
        @Header("Authorization") token: String,
        @Body request: CreateUserRequest
    ): Response<Unit>

    @POST("/api/v1/game-sessions")
    suspend fun createGameSession(
        @Header("Authorization") token: String,
        @Body request: GameSessionCreateRequest
    ): Response<Unit>

    @GET("/api/v1/new-session-parameters")
    suspend fun getNewGameParameters(
        @Header("Authorization") token: String,
        @Query("from_level") fromLevel: Int? // <-- ADD THIS PARAMETER
    ): Response<GameParametersResponse>

    @GET("/api/v1/users/me")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @GET("/api/v1/users/me/stats")
    suspend fun getUserStats(
        @Header("Authorization") token: String
    ): Response<UserStatsResponse>

    @POST("/api/v1/journals")
    suspend fun createJournal(
        @Header("Authorization") token: String,
        @Body request: DailyJournalCreateRequest
    ): Response<DailyJournalResponse>

    @GET("/api/v1/journals")
    suspend fun getMyJournals(
        @Header("Authorization") token: String
    ): Response<List<DailyJournalResponse>>
}