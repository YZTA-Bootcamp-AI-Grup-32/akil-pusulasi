package com.example.akilpusulasi.network.request

import com.google.gson.annotations.SerializedName

data class GameSessionCreateRequest(
    @SerializedName("game_name")
    val gameName: String,

    @SerializedName("score")
    val score: Int,

    @SerializedName("duration_seconds")
    val durationSeconds: Int,

    @SerializedName("difficulty_level")
    val difficultyLevel: Int
)