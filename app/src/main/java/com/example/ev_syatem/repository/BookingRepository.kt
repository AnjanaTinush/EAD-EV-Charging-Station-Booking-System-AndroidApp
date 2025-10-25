package com.example.ev_syatem.repository

import com.example.ev_syatem.data.Booking
import com.example.ev_syatem.data.Station
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class BookingRepository {

    private val client = OkHttpClient()
    private val baseUrl = "http://172.28.16.189:8080/api/booking"

    // Create new booking
    fun createBooking(booking: Booking, callback: (Boolean, String) -> Unit) {
        val json = JSONObject().apply {
            put("ownerNIC", booking.ownerNIC)
            put("stationId", booking.stationId)
            put("reservationTime", booking.reservationTime)
        }

        val body = RequestBody.create("application/json".toMediaType(), json.toString())

        val request = Request.Builder()
            .url(baseUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val message = if (response.isSuccessful) {
                    "Booking successful!"
                } else {
                    "Failed: ${response.message}"
                }
                callback(response.isSuccessful, message)
            }
        })
    }

    // Get upcoming bookings (Pending & Approved)
    fun getUpcomingBookings(ownerNIC: String, callback: (List<Booking>) -> Unit) {
        val request = Request.Builder()
            .url("$baseUrl/upcoming?ownerNic=$ownerNIC")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body.isNullOrEmpty()) {
                    callback(emptyList())
                    return
                }

                val bookings = parseBookings(body)
                callback(bookings)
            }
        })
    }

    // Get booking history (Completed & Cancelled)
    fun getBookingHistory(ownerNIC: String, callback: (List<Booking>) -> Unit) {
        val request = Request.Builder()
            .url("$baseUrl/history?ownerNic=$ownerNIC")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body.isNullOrEmpty()) {
                    callback(emptyList())
                    return
                }

                val bookings = parseBookings(body)
                callback(bookings)
            }
        })
    }

    // Update booking time
    fun updateBooking(bookingId: String, newReservationTime: String, callback: (Boolean, String) -> Unit) {
        val json = JSONObject().apply {
            put("newReservationTime", newReservationTime)
        }

        val body = RequestBody.create("application/json".toMediaType(), json.toString())

        val request = Request.Builder()
            .url("$baseUrl/$bookingId")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val message = if (response.isSuccessful) {
                    "Booking updated successfully"
                } else {
                    response.body?.string() ?: "Failed to update"
                }
                callback(response.isSuccessful, message)
            }
        })
    }

    // Cancel booking
    fun cancelBooking(bookingId: String, reason: String, callback: (Boolean, String) -> Unit) {
        val json = JSONObject().apply {
            put("reason", reason)
        }

        val body = RequestBody.create("application/json".toMediaType(), json.toString())

        val request = Request.Builder()
            .url("$baseUrl/$bookingId/cancel")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val message = if (response.isSuccessful) {
                    "Booking cancelled"
                } else {
                    response.body?.string() ?: "Failed to cancel"
                }
                callback(response.isSuccessful, message)
            }
        })
    }

    // Get active stations
    fun getActiveStations(callback: (List<Station>) -> Unit) {
        val request = Request.Builder()
            .url("http://172.28.16.189:8080/api/Station")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body.isNullOrEmpty()) {
                    callback(emptyList())
                    return
                }

                val jsonArray = JSONArray(body)
                val stations = mutableListOf<Station>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    if (obj.optBoolean("isActive", false)) {
                        stations.add(
                            Station(
                                id = obj.getString("id"),
                                name = obj.getString("name"),
                                location = obj.getString("location"),
                                type = obj.getString("type"),
                                availableSlots = obj.getInt("availableSlots"),
                                isActive = obj.getBoolean("isActive")
                            )
                        )
                    }
                }
                callback(stations)
            }
        })
    }

    // Check availability
    fun checkAvailability(stationId: String, reservationTime: String, callback: (isAvailable: Boolean, message: String) -> Unit) {
        val request = Request.Builder()
            .url("http://172.28.16.189:8080/api/booking/all")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body.isNullOrEmpty()) {
                    callback(false, "Could not verify availability.")
                    return
                }

                try {
                    val allBookings = JSONArray(body)
                    var isSlotTaken = false

                    for (i in 0 until allBookings.length()) {
                        val booking = allBookings.getJSONObject(i)
                        val bookingStationId = booking.getString("stationId")
                        val bookingTime = booking.getString("reservationTime")
                        val status = booking.getString("status")

                        if (bookingStationId == stationId &&
                            bookingTime == reservationTime &&
                            (status == "Pending" || status == "Completed")) {
                            isSlotTaken = true
                            break
                        }
                    }

                    if (isSlotTaken) {
                        callback(false, "Slot is already booked.")
                    } else {
                        callback(true, "Slot is available.")
                    }

                } catch (e: Exception) {
                    callback(false, "Error parsing booking data.")
                }
            }
        })
    }

    // Helper function to parse bookings JSON
    private fun parseBookings(jsonString: String): List<Booking> {
        val bookings = mutableListOf<Booking>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                bookings.add(
                    Booking(
                        id = obj.getString("id"),
                        ownerNIC = obj.getString("ownerNIC"),
                        stationId = obj.getString("stationId"),
                        reservationTime = obj.getString("reservationTime"),
                        status = obj.getString("status"),
                        qrCodeBase64 = obj.optString("qrCodeBase64", null),
                        createdAt = obj.optString("createdAt", null),
                        updatedAt = obj.optString("updatedAt", null)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bookings
    }
}