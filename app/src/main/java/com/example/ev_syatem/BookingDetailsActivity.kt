package com.example.ev_syatem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class BookingDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_details)

        // Find views
        val stationIdTv: TextView = findViewById(R.id.details_station_id)
        val reservationTimeTv: TextView = findViewById(R.id.details_reservation_time)
        val statusTv: TextView = findViewById(R.id.details_status)
        val qrCodeCard: CardView = findViewById(R.id.qr_code_card)
        val qrCodeImage: ImageView = findViewById(R.id.qr_code_image)
        val backButton: CardView = findViewById(R.id.btn_back_details)

        // Get data from Intent
        val stationId = intent.getStringExtra("STATION_ID")
        val reservationTime = intent.getStringExtra("RESERVATION_TIME")
        val status = intent.getStringExtra("STATUS")
        val qrCodeBase64 = intent.getStringExtra("QR_CODE")

        // Set data to views
        stationIdTv.text = stationId
        reservationTimeTv.text = reservationTime
        statusTv.text = status

        // Handle QR Code visibility and decoding
        if (qrCodeBase64 != null) {
            try {
                val decodedString = Base64.decode(qrCodeBase64, Base64.DEFAULT)
                val bitmap: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                qrCodeImage.setImageBitmap(bitmap)
                qrCodeCard.visibility = View.VISIBLE
            } catch (e: IllegalArgumentException) {
                // Handle error if Base64 string is invalid
                qrCodeCard.visibility = View.GONE
            }
        } else {
            qrCodeCard.visibility = View.GONE
        }

        // Handle back button click
        backButton.setOnClickListener {
            finish()
        }
    }
}
