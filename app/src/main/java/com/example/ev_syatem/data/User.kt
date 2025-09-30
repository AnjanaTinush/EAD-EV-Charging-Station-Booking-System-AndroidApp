package com.example.ev_syatem.data

data class User(
    val id: Int = 0, // Auto-generated ID
    val nic: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val isActivate: Boolean = false
)