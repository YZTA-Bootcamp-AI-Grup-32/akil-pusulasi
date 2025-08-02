package com.example.akilpusulasi.network.response

import com.google.gson.annotations.SerializedName

data class GameScoreData(
    @SerializedName("score")
    val score: Int,

    @SerializedName("level")
    val level: Int
)

data class UserStatsResponse(
    @SerializedName("journal_streak")
    val journalStreak: Int,

    @SerializedName("last_five_scores")
    val lastFiveScores: List<Int>,

    @SerializedName("last_ten_game_stats")
    val lastTenGameStats: List<GameScoreData> // New field for the graph
)