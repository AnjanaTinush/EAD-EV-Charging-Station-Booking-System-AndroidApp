package com.example.ev_syatem.data

data class Station(
    val id: String,
    val name: String,
    val location: String,
    val type: String,
    val availableSlots: Int,
    val isActive: Boolean
)
