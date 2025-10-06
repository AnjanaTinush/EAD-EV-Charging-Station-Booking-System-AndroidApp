package com.example.ev_syatem

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    private lateinit var nicInput: TextInputEditText
    private lateinit var fullNameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var loginLink: TextView

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // ✅ Setup system UI
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = getColor(R.color.primary_green)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        setContentView(R.layout.activity_register)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        nicInput = findViewById(R.id.nic_input)
        fullNameInput = findViewById(R.id.fullname_input)
        emailInput = findViewById(R.id.email_input)
        phoneInput = findViewById(R.id.phone_input)
        passwordInput = findViewById(R.id.password_input)
        confirmPasswordInput = findViewById(R.id.confirm_password_input)
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
        val confirmPassword = confirmPasswordInput.text.toString()

        nicInput.error = null
        fullNameInput.error = null
        emailInput.error = null
        phoneInput.error = null
        passwordInput.error = null
        confirmPasswordInput.error = null

        var isValid = true

        if (nic.isEmpty()) {
            nicInput.error = "NIC is required"
            isValid = false
        } else if (nic.length < 10) {
            nicInput.error = "NIC must be at least 10 characters"
            isValid = false
        }

        if (fullName.isEmpty()) {
            fullNameInput.error = "Full name is required"
            isValid = false
        }

        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Invalid email address"
            isValid = false
        }

        if (phone.isEmpty()) {
            phoneInput.error = "Phone is required"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordInput.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.error = "Confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordInput.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun registerUser() {
        val nic = nicInput.text.toString().trim()
        val fullName = fullNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val password = passwordInput.text.toString()

        // ✅ Always include default role = "EvOwner"
        val jsonBody = JSONObject().apply {
            put("nic", nic)
            put("fullName", fullName)
            put("email", email)
            put("phone", phone)
            put("password", password)
            put("role", "EvOwner")
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://10.0.2.2:8080/api/auth/register") // ✅ Correct API endpoint
            .post(requestBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration successful! Please log in.",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val message = try {
                            val errorJson = JSONObject(responseBody ?: "")
                            errorJson.optString("message", "Registration failed")
                        } catch (_: Exception) {
                            responseBody ?: "Registration failed"
                        }

                        Toast.makeText(
                            this@RegisterActivity,
                            "Failed: $message",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
