package com.example.akilpusulasi.network.response

import com.google.gson.annotations.SerializedName

data class UserStatsResponse(
    @SerializedName("journal_streak")
    val journalStreak: Int,

    @SerializedName("last_five_scores")
    val lastFiveScores: List<Int>
)
