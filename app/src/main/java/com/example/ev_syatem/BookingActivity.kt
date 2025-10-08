package com.example.ev_syatem

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ev_syatem.data.Booking
import com.example.ev_syatem.repository.BookingRepository

class BookingActivity : AppCompatActivity() {

    private lateinit var etOwnerNIC: EditText
    private lateinit var etStationId: EditText
    private lateinit var etReservationTime: EditText
    private lateinit var btnCreateBooking: Button
    private val bookingRepository = BookingRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContentView(R.layout.activity_booking)

        etOwnerNIC = findViewById(R.id.et_owner_nic)
        etStationId = findViewById(R.id.et_station_id)
        etReservationTime = findViewById(R.id.et_reservation_time)
        btnCreateBooking = findViewById(R.id.btn_create_booking)

        btnCreateBooking.setOnClickListener {
            val ownerNIC = etOwnerNIC.text.toString().trim()
            val stationId = etStationId.text.toString().trim()
            val reservationTime = etReservationTime.text.toString().trim()

            if (ownerNIC.isEmpty() || stationId.isEmpty() || reservationTime.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val booking = Booking(ownerNIC, stationId, reservationTime)
            bookingRepository.createBooking(booking) { success, message ->
                runOnUiThread {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    if (success) finish()
                }
            }
        }
    }
}
