package com.example.ev_syatem // ✅ FIX: Corrected package name

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
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
    private lateinit var tvAvailabilityStatus: TextView

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
        dateCard = findViewById(R.id.date_card)
        timeCard = findViewById(R.id.time_card)
        tvAvailabilityStatus = findViewById(R.id.tv_availability_status)

        setupListeners()
        loadActiveStations()
    }

    private fun setupListeners() {
        dateCard.setOnClickListener { showDatePicker() }
        timeCard.setOnClickListener { showTimePicker() }

        etOwnerNIC.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkSlotAvailability()
            }
        })

        // This button will now show the summary dialog
        btnCreateBooking.setOnClickListener {
            showBookingSummaryDialog()
        }
    }

    // ✅ NEW: Shows the booking summary dialog
    private fun showBookingSummaryDialog() {
        val nic = etOwnerNIC.text.toString().trim()
        val station = stations.find { it.id == selectedStationId }
        val dateStr = tvDate.text.toString()
        val timeStr = tvTime.text.toString()

        if (nic.length !in 10..12) {
            Toast.makeText(this, "NIC must be 10–12 characters", Toast.LENGTH_SHORT).show()
            return
        }
        if (station == null || selectedDateTime == null) {
            Toast.makeText(this, "Please select station, date & time", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.dialog_booking_summary, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = dialogBuilder.create()

        // Set transparent background to show custom shape
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Find views inside the dialog
        val summaryStationName: TextView = dialogView.findViewById(R.id.summary_station_name)
        val summaryDate: TextView = dialogView.findViewById(R.id.summary_date)
        val summaryTime: TextView = dialogView.findViewById(R.id.summary_time)
        val btnCancel: Button = dialogView.findViewById(R.id.btn_cancel)
        val btnConfirmBooking: Button = dialogView.findViewById(R.id.btn_confirm_booking)

        // Populate dialog with data
        summaryStationName.text = station.name
        summaryDate.text = dateStr
        summaryTime.text = timeStr

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmBooking.setOnClickListener {
            // On confirm, proceed with booking creation
            performBookingCreation(nic, station.id, selectedDateTime!!)
            dialog.dismiss()
        }

        dialog.show()
    }

    // ✅ NEW: Contains the actual booking logic, moved from the original click listener
    private fun performBookingCreation(nic: String, stationId: String, dateTime: String) {
        // ✅ FIX: Pass the 'dateTime' parameter to the Booking constructor
        val booking = Booking(
            ownerNIC = nic,
            stationId = stationId,
            reservationTime = dateTime
        )

        btnCreateBooking.isEnabled = false
        btnCreateBooking.text = "Processing..."

        bookingRepository.createBooking(booking) { success, message ->
            runOnUiThread {
                btnCreateBooking.isEnabled = true
                btnCreateBooking.text = "Confirm Booking"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                if (success) {
                    finish()
                }
            }
        }
    }
    private fun loadActiveStations() {
        bookingRepository.getActiveStations { fetched ->
            runOnUiThread {
                stations.clear()
                stations.addAll(fetched)
                if (stations.isEmpty()) {
                    Toast.makeText(this, "No active stations available", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                val adapter = ArrayAdapter(
                    this,
                    R.layout.spinner_selected_item_style,
                    stations.map { "${it.name} - ${it.location}" }
                )
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_style)
                spinnerStation.adapter = adapter

                spinnerStation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedStationId = stations[position].id
                        checkSlotAvailability()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedStationId = null
                        checkSlotAvailability()
                    }
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
                tvDate.text = formatted
                updateDateTime()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // ✅ NEW: Set the 7-day booking window validation
        // Set minimum date to today
        datePicker.datePicker.minDate = System.currentTimeMillis()

        // Set maximum date to 7 days from today
        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.DAY_OF_MONTH, 7)
        datePicker.datePicker.maxDate = maxDate.timeInMillis

        datePicker.show()
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
            checkSlotAvailability()
        } else {
            tvAvailabilityStatus.visibility = View.GONE
        }
    }

    private fun checkSlotAvailability() {
        val stationId = selectedStationId
        val dateTime = selectedDateTime

        if (stationId == null || dateTime == null) {
            tvAvailabilityStatus.visibility = View.GONE
            btnCreateBooking.isEnabled = false
            return
        }

        tvAvailabilityStatus.visibility = View.VISIBLE
        tvAvailabilityStatus.text = "Checking availability..."
        tvAvailabilityStatus.setTextColor(Color.GRAY)
        btnCreateBooking.isEnabled = false

        bookingRepository.checkAvailability(stationId, dateTime) { isAvailable, message ->
            runOnUiThread {
                tvAvailabilityStatus.text = message
                if (isAvailable) {
                    tvAvailabilityStatus.setTextColor(Color.parseColor("#10B981"))
                    // Only enable the button if the NIC is also valid
                    btnCreateBooking.isEnabled = etOwnerNIC.text.toString().trim().length in 10..12
                } else {
                    tvAvailabilityStatus.setTextColor(Color.RED)
                    btnCreateBooking.isEnabled = false
                }
            }
        }
    }

    private fun validateInputs() {
        checkSlotAvailability()
    }
}
