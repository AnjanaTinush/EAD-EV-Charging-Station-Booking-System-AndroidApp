package com.example.ev_syatem

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ev_syatem.database.DatabaseHelper
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
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // Setup immersive UI
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = getColor(R.color.primary_green_dark)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)
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
            if (validateLoginForm()) {
                performLogin()
            }
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

        // Set button to loading state
        loginButton.isEnabled = false
        loginButton.text = "Signing In..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()

                val jsonBody = JSONObject().apply {
                    put("nic", nic)
                    put("password", password)
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

                // Correct API endpoint for mobile login (IIS hosted)
                val request = Request.Builder()
                    .url("http://172.20.10.9:8080/api/auth/login?platform=mobile")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(responseBody)
                        val userObj = jsonResponse.optJSONObject("user")

                        if (userObj != null) {
                            val id = userObj.optString("id", "")
                            val username = userObj.optString("username", "")
                            val email = userObj.optString("email", "")
                            val phone = userObj.optString("phone", "")
                            val role = userObj.optString("role", "")
                            val isActive = userObj.optBoolean("isActive", true)
                            val nicFromServer = userObj.optString("nic", "")

                            // Check if account is deactivated
                            if (!isActive) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Your account is deactivated. Please contact admin.",
                                    Toast.LENGTH_LONG
                                ).show()
                                // Re-enable button text before return
                                loginButton.isEnabled = true
                                loginButton.text = "Login"
                                return@withContext
                            }

                            // Store minimal user info locally (clear previous session)
                            dbHelper.clearUsers()
                            dbHelper.insertUser(
                                serverId = id,
                                username = username,
                                email = email,
                                phone = phone,
                                nic = nicFromServer,
                                role = role,
                                isActive = isActive,
                                createdAt = System.currentTimeMillis().toString()
                            )

                            Toast.makeText(
                                this@LoginActivity,
                                "Welcome, $username!",
                                Toast.LENGTH_LONG
                            ).show()

                            // ✅ Navigate based on role
                            val next = if (role.equals("StationOperator", ignoreCase = true)) {
                                StationOperatorHomeActivity::class.java
                            } else {
                                HomeActivity::class.java
                            }

                            val intent = Intent(this@LoginActivity, next)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Invalid server response.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val msg = try {
                            val errorJson = JSONObject(responseBody)
                            errorJson.optString("message", "Invalid credentials")
                        } catch (_: Exception) {
                            "Invalid credentials"
                        }

                        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login failed: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                // Always re-enable button after attempt
                withContext(Dispatchers.Main) {
                    loginButton.isEnabled = true
                    loginButton.text = "Login"
                }
            }
        }
    }
}
