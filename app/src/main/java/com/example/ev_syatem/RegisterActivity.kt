package com.example.ev_syatem

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ev_syatem.data.User
import com.example.ev_syatem.repository.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var nicInput: TextInputEditText
    private lateinit var fullNameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var loginLink: TextView
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure no action bar is shown
        supportActionBar?.hide()

        // Set status bar color to match green header
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = getColor(R.color.primary_green)

        // Make status bar content light (for dark background)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.isAppearanceLightStatusBars = false
        }

        setContentView(R.layout.activity_register)

        // Initialize repository
        userRepository = UserRepository(this)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        nicInput = findViewById(R.id.nic_input)
        fullNameInput = findViewById(R.id.fullname_input)
        emailInput = findViewById(R.id.email_input)
        phoneInput = findViewById(R.id.phone_input)
        passwordInput = findViewById(R.id.password_input)
        registerButton = findViewById(R.id.register_button)
        loginLink = findViewById(R.id.login_link)
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            if (validateForm()) {
                registerUser()
            }
        }

        loginLink.setOnClickListener {
            // Navigate back to login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateForm(): Boolean {
        val nic = nicInput.text.toString().trim()
        val fullName = fullNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val password = passwordInput.text.toString()

        // Reset errors
        nicInput.error = null
        fullNameInput.error = null
        emailInput.error = null
        phoneInput.error = null
        passwordInput.error = null

        var isValid = true

        // Validate NIC
        if (nic.isEmpty()) {
            nicInput.error = "NIC is required"
            isValid = false
        } else if (nic.length < 10) {
            nicInput.error = "NIC must be at least 10 characters"
            isValid = false
        }

        // Validate Full Name
        if (fullName.isEmpty()) {
            fullNameInput.error = "Full name is required"
            isValid = false
        } else if (fullName.length < 2) {
            fullNameInput.error = "Full name must be at least 2 characters"
            isValid = false
        }

        // Validate Email
        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Please enter a valid email address"
            isValid = false
        }

        // Validate Phone
        if (phone.isEmpty()) {
            phoneInput.error = "Phone number is required"
            isValid = false
        } else if (phone.length < 10) {
            phoneInput.error = "Phone number must be at least 10 digits"
            isValid = false
        }

        // Validate Password
        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordInput.error = "Password must be at least 6 characters"
            isValid = false
        }

        return isValid
    }

    private fun registerUser() {
        // Get form data
        val nic = nicInput.text.toString().trim()
        val fullName = fullNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val password = passwordInput.text.toString()

        // Check if NIC already exists
        if (userRepository.isNicExists(nic)) {
            nicInput.error = "This NIC is already registered"
            Toast.makeText(this, "NIC already exists. Please use a different NIC.", Toast.LENGTH_LONG).show()
            return
        }

        // Check if email already exists
        if (userRepository.isEmailExists(email)) {
            emailInput.error = "This email is already registered"
            Toast.makeText(this, "Email already exists. Please use a different email.", Toast.LENGTH_LONG).show()
            return
        }

        // Create User object with isActive = true (automatically activated)
        val user = User(
            nic = nic,
            fullName = fullName,
            email = email,
            phone = phone,
            password = password,
            isActive = true  // Automatically activate new accounts
        )

        // Register user in local database
        val result = userRepository.registerUser(user)

        if (result > 0) {
            Toast.makeText(this, "Registration successful! Please login with your credentials.", Toast.LENGTH_LONG).show()

            // Navigate to login page
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}