package com.example.ev_syatem.data

data class User(
    val id: Int = 0, // Auto-generated ID
    val nic: String, // NIC is used as primary key for EV Owners
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val isActive: Boolean = true, // Changed from isActivate to match backend
    val role: String = "EvOwner", // User role: Backoffice, StationOperator, EvOwner
    val createdAt: String = "",
    val updatedAt: String = ""
)