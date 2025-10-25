package com.example.ev_syatem.repository

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class FinancialRepository {

    private val client = OkHttpClient()
    // Make sure to use the correct IP address for your local server
    private val baseUrl = "http://172.28.16.189:8080/api/financial"

    fun createFinancialRecord(
        username: String,
        nic: String,
        amount: Double,
        paymentType: String,
        callback: (Boolean, String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("username", username)
            put("nic", nic)
            put("amount", amount)
            put("paymentType", paymentType)
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
                val responseBody = response.body?.string()
                val message = if (response.isSuccessful) {
                    "Payment recorded successfully"
                } else {
                    try {
                        JSONObject(responseBody).getString("message")
                    } catch (e: Exception) {
                        "Failed to record payment. Status: ${response.code}"
                    }
                }
                callback(response.isSuccessful, message)
            }
        })
    }
}
