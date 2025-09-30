package com.example.ev_syatem

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ev_syatem.repository.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var nicInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerLink: TextView
    private lateinit var forgotPasswordLink: TextView
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure no action bar is shown
        supportActionBar?.hide()

        // Set status bar color to match background
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = getColor(R.color.background_primary)

        // Make status bar content dark (for light background)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.isAppearanceLightStatusBars = true
        }

        setContentView(R.layout.activity_login)

        userRepository = UserRepository(this)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        nicInput = findViewById(R.id.nicInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        registerLink = findViewById(R.id.register_link)
        forgotPasswordLink = findViewById(R.id.forgot_password)
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            if (validateLoginForm()) {
                performLogin()
            }
        }

        registerLink.setOnClickListener {
            // Navigate to registration activity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordLink.setOnClickListener {
            // TODO: Implement forgot password functionality
            // For now, just show a placeholder message
        }
    }

    private fun validateLoginForm(): Boolean {
        val nic = nicInput.text.toString().trim()
        val password = passwordInput.text.toString()

        // Reset errors
        nicInput.error = null
        passwordInput.error = null

        var isValid = true

        // Validate NIC
        if (nic.isEmpty()) {
            nicInput.error = "NIC is required"
            isValid = false
        }

        // Validate Password
        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            isValid = false
        }

        return isValid
    }

    private fun performLogin() {
        val nic = nicInput.text.toString().trim()
        val password = passwordInput.text.toString()

        // Authenticate user from local database
        val user = userRepository.loginUser(nic, password)

        if (user != null) {
            // Check if user account is activated
            if (!user.isActivate) {
                Toast.makeText(this, "Your account is pending activation. Please contact admin.", Toast.LENGTH_LONG).show()
                return
            }

            // Login successful
            Toast.makeText(this, "Welcome back, ${user.fullName}!", Toast.LENGTH_SHORT).show()

            // Save user session
            getSharedPreferences("EV_PREFS", MODE_PRIVATE)
                .edit()
                .putString("USER_NIC", nic)
                .apply()

            // Navigate to home
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            // Login failed
            Toast.makeText(this, "Invalid NIC or password. Please try again.", Toast.LENGTH_LONG).show()
            passwordInput.error = "Invalid credentials"
        }
    }
}
