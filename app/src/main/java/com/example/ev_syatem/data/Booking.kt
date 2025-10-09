package com.example.ev_syatem.data

data class Booking(
    val id: String = "",
    val ownerNIC: String,
    val stationId: String,
    val reservationTime: String,
    val status: String = "Pending", // Pending, Approved, Completed, Cancelled
    val qrCodeBase64: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

enum class BookingStatus(val displayName: String, val color: String) {
    PENDING("Pending", "#F59E0B"),
    APPROVED("Approved", "#22C55E"),
    COMPLETED("Completed", "#3B82F6"),
    CANCELLED("Cancelled", "#EF4444")
}