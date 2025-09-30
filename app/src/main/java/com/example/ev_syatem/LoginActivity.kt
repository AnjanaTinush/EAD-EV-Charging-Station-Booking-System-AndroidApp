package com.example.ev_syatem

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var nicInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerLink: TextView
    private lateinit var forgotPasswordLink: TextView

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

        // TODO: Implement actual login logic here
        // For now, we'll just navigate to MainActivity

        // You can add your API call here to authenticate the user
        // Example data structure:
        /*
        val loginData = mapOf(
            "nic" to nic,
            "password" to password
        )
        */

        // For demonstration, navigate to main activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
