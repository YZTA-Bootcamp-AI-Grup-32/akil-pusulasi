package com.example.akilpusulasi.network.request

data class CreateUserRequest(
    val fullName: String,
    val birthYear: Int,
    val interests: List<String>
)