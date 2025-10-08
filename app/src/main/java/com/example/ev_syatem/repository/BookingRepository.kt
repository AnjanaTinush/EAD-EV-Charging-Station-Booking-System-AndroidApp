package com.example.ev_syatem.repository

import com.example.ev_syatem.data.Booking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class BookingRepository {

    private val client = OkHttpClient()
    private val baseUrl = "http://10.0.2.2:8080/api/booking"

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
}
