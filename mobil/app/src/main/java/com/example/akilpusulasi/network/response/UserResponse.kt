package com.example.akilpusulasi.network.response

import com.google.gson.annotations.SerializedName

// This class should match the UserResponse schema in your backend
data class UserResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("birth_year")
    val birthYear: Int,

    @SerializedName("interests")
    val interests: List<String>,

    @SerializedName("created_at")
    val createdAt: String
)