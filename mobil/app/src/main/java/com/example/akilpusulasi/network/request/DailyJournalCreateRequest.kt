package com.example.akilpusulasi.network.request

import com.google.gson.annotations.SerializedName

// This class must match the DailyJournalCreate schema in your FastAPI backend
data class DailyJournalCreateRequest(
    @SerializedName("journal_content")
    val journalContent: String
)