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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var nicInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerLink: TextView
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = getColor(R.color.primary_green_dark)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        setContentView(R.layout.activity_login)
        userRepository = UserRepository(this)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        nicInput = findViewById(R.id.nic_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        registerLink = findViewById(R.id.register_link)
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            if (validateLoginForm()) performLogin()
        }
        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateLoginForm(): Boolean {
        val nic = nicInput.text.toString().trim()
        val password = passwordInput.text.toString()

        nicInput.error = null
        passwordInput.error = null

        var isValid = true
        if (nic.isEmpty()) {
            nicInput.error = "NIC is required"
            isValid = false
        }
        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            isValid = false
        }
        return isValid
    }

    private fun performLogin() {
        val nic = nicInput.text.toString().trim()
        val password = passwordInput.text.toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()

                val jsonBody = JSONObject()
                jsonBody.put("nic", nic)
                jsonBody.put("password", password)

                val requestBody = jsonBody.toString()
                    .toRequestBody("application/json".toMediaType())

                // --- IMPORTANT: use 10.0.2.2 for emulator to reach host IIS ---
                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/api/mobileauth/login")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Login successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        getSharedPreferences("EV_PREFS", MODE_PRIVATE)
                            .edit()
                            .putString("USER_NIC", nic)
                            .apply()

                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Invalid NIC or password. Server returned ${response.code}",
                            Toast.LENGTH_LONG
                        ).show()
                        passwordInput.error = "Invalid credentials"
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
