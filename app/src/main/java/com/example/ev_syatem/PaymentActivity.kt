package com.example.ev_syatem

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ev_syatem.repository.FinancialRepository
import com.google.android.material.textfield.TextInputEditText

class PaymentActivity : AppCompatActivity() {

    private lateinit var bookingIdLabel: TextView
    // ✅ FIX: Changed to TextInputEditText to match the layout
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var nicEditText: TextInputEditText
    private lateinit var amountEditText: TextInputEditText
    private lateinit var paymentTypeEditText: TextInputEditText
    private lateinit var submitButton: Button

    private val financialRepository = FinancialRepository()
    private var bookingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        bookingId = intent.getStringExtra("BOOKING_ID")

        initializeViews()
        setupListeners()

        if (bookingId == null) {
            Toast.makeText(this, "Error: No Booking ID found.", Toast.LENGTH_LONG).show()
            finish()
        } else {
            bookingIdLabel.text = "Booking ID: $bookingId"
        }
    }

    private fun initializeViews() {
        // ✅ FIX: This is the correct way to initialize the views from your layout
        bookingIdLabel = findViewById(R.id.tv_booking_id_label)
        usernameEditText = findViewById(R.id.et_username)
        nicEditText = findViewById(R.id.et_nic)
        amountEditText = findViewById(R.id.et_amount)
        paymentTypeEditText = findViewById(R.id.et_payment_type)
        submitButton = findViewById(R.id.btn_submit_payment)
    }

    private fun setupListeners() {
        submitButton.setOnClickListener {
            if (validateInputs()) {
                submitPayment()
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (usernameEditText.text.toString().isBlank()) {
            usernameEditText.error = "Username is required"
            return false
        }
        if (nicEditText.text.toString().isBlank()) {
            nicEditText.error = "NIC is required"
            return false
        }
        if (amountEditText.text.toString().isBlank()) {
            amountEditText.error = "Amount is required"
            return false
        }
        if (paymentTypeEditText.text.toString().isBlank()) {
            paymentTypeEditText.error = "Payment Type is required"
            return false
        }
        return true
    }

    private fun submitPayment() {
        val username = usernameEditText.text.toString()
        val nic = nicEditText.text.toString()
        val amount = amountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val paymentType = paymentTypeEditText.text.toString()

        submitButton.isEnabled = false
        submitButton.text = "Submitting..."

        financialRepository.createFinancialRecord(username, nic, amount, paymentType) { success, message ->
            runOnUiThread {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                submitButton.isEnabled = true
                submitButton.text = "Submit Payment"
                if (success) {
                    finish() // Close the payment screen on success
                }
            }
        }
    }
}
