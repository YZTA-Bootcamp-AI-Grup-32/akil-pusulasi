package com.example.akilpusulasi.network.response

import com.google.gson.annotations.SerializedName

// This class must match the DailyJournalResponse schema in your FastAPI backend
data class DailyJournalResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("journal_content")
    val journalContent: String,

    @SerializedName("ai_prompt")
    val aiPrompt: String?, // Can be null

    @SerializedName("ai_response")
    val aiResponse: String?, // Can be null

    @SerializedName("created_at")
    val createdAt: String
)