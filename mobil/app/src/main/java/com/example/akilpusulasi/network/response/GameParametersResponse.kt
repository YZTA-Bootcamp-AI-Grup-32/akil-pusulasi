package com.example.akilpusulasi.network.response

import com.google.gson.annotations.SerializedName

data class GameParametersResponse(
    @SerializedName("difficulty_level")
    val difficultyLevel: Int,

    @SerializedName("pattern_size")
    val patternSize: Int,

    @SerializedName("grid_size")
    val gridSize: Int, // This is the total number of cells (e.g., 9, 16)

    @SerializedName("display_time_ms")
    val displayTimeMs: Int
)