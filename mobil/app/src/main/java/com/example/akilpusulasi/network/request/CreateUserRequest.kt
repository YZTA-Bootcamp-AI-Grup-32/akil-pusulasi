package com.example.akilpusulasi.network.request

import com.google.gson.annotations.SerializedName

data class CreateUserRequest(
    @SerializedName("full_name")
    val fullName: String,

    // Tell Gson to use "birth_year" as the key in the final JSON
    @SerializedName("birth_year")
    val birthYear: Int,
    val interests: List<String>
)