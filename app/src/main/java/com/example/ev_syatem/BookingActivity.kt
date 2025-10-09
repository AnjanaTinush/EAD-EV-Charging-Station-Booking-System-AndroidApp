package com.example.ev_syatem

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ev_syatem.data.Booking
import com.example.ev_syatem.data.Station
import com.example.ev_syatem.repository.BookingRepository
import java.text.SimpleDateFormat
import java.util.*

class BookingActivity : AppCompatActivity() {

    private lateinit var etOwnerNIC: EditText
    private lateinit var spinnerStation: Spinner
    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView
    private lateinit var btnCreateBooking: Button

    // New variables for the CardViews
    private lateinit var dateCard: CardView
    private lateinit var timeCard: CardView

    private val bookingRepository = BookingRepository()
    private val stations = mutableListOf<Station>()
    private var selectedStationId: String? = null
    private var selectedDateTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContentView(R.layout.activity_booking)

        etOwnerNIC = findViewById(R.id.et_owner_nic)
        spinnerStation = findViewById(R.id.spinner_station)
        tvDate = findViewById(R.id.tv_date)
        tvTime = findViewById(R.id.tv_time)
        btnCreateBooking = findViewById(R.id.btn_create_booking)

        // Find the CardViews by their IDs
        dateCard = findViewById(R.id.date_card)
        timeCard = findViewById(R.id.time_card)

        setupListeners()
        loadActiveStations()
    }

    private fun setupListeners() {
        // Set listeners on the CardViews instead of the TextViews
        dateCard.setOnClickListener { showDatePicker() }
        timeCard.setOnClickListener { showTimePicker() }

        btnCreateBooking.setOnClickListener {
            val nic = etOwnerNIC.text.toString().trim()
            if (nic.length !in 10..12) {
                Toast.makeText(this, "NIC must be 10–12 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedStationId == null || selectedDateTime == null) {
                Toast.makeText(this, "Please select station, date & time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val booking = Booking(nic, selectedStationId!!, selectedDateTime!!)
            btnCreateBooking.isEnabled = false
            btnCreateBooking.text = "Processing..."

            bookingRepository.createBooking(booking) { success, message ->
                runOnUiThread {
                    btnCreateBooking.isEnabled = true
                    btnCreateBooking.text = "Confirm Booking"
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    if (success) finish()
                }
            }
        }
    }

    // ... The rest of your functions (loadActiveStations, showDatePicker, etc.) remain the same ...

    private fun loadActiveStations() {
        bookingRepository.getActiveStations { fetched ->
            runOnUiThread {
                stations.clear()
                stations.addAll(fetched)
                if (stations.isEmpty()) {
                    Toast.makeText(this, "No active stations available", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                // Step 1: Use the layout for the selected item view
                val adapter = ArrayAdapter(
                    this,
                    R.layout.spinner_selected_item_style, // For the visible, selected item
                    stations.map { "${it.name} - ${it.location}" }
                )

                // Step 2: Set the layout for the dropdown items
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_style) // For items in the list

                spinnerStation.adapter = adapter
                spinnerStation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
                    ) {
                        selectedStationId = stations[position].id
                        validateInputs()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
                tvDate.text = formatted
                updateDateTime()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                val formatted = String.format("%02d:%02d", hour, minute)
                tvTime.text = formatted
                updateDateTime()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateTime() {
        val date = tvDate.text.toString()
        val time = tvTime.text.toString()
        if (date.contains("-") && time.contains(":")) {
            val dateTime = "${date}T${time}:00Z"
            selectedDateTime = dateTime
            validateInputs()
        }
    }

    private fun validateInputs() {
        btnCreateBooking.isEnabled =
            etOwnerNIC.text.toString().trim().length in 10..12 &&
                    selectedStationId != null &&
                    selectedDateTime != null
    }
}
