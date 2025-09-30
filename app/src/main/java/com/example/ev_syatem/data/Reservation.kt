package com.example.ev_syatem.data

data class Reservation(
    val id: Int = 0, // Auto-generated ID
    val userNic: String, // Foreign key to User
    val stationName: String,
    val date: String,
    val time: String,
    val status: String, // "PENDING", "APPROVED", "REJECTED", "COMPLETED"
    val createdAt: Long = System.currentTimeMillis()
)