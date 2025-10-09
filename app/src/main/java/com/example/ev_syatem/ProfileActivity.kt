package com.example.ev_syatem

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ev_syatem.database.DatabaseHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
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

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileNameText: TextView
    private lateinit var profileNicText: TextView
    private lateinit var fullNameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var editToggleButton: MaterialButton
    private lateinit var saveProfileButton: MaterialButton
    private lateinit var logoutButton: MaterialButton
    private lateinit var deactivateAccountButton: MaterialButton
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var dbHelper: DatabaseHelper
    private var userNic: String = ""
    private var userId: String = ""   // 🔹 actual MongoDB ID (we’ll load from local user data if stored)
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContentView(R.layout.activity_profile)

        dbHelper = DatabaseHelper(this)

        val user = dbHelper.getLatestUser()
        if (user != null) {
            userNic = user["nic"] ?: ""
            profileNameText = findViewById(R.id.profile_name_text)
            profileNicText = findViewById(R.id.profile_nic_text)
            profileNameText.text = user["full_name"]
            profileNicText.text = user["nic"]
        } else {
            navigateToLogin()
            return
        }

        initializeViews()
        setupBottomNavigation()
        loadUserData()
        setupClickListeners()
    }

    private fun initializeViews() {
        fullNameInput = findViewById(R.id.fullNameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        editToggleButton = findViewById(R.id.edit_toggle_button)
        saveProfileButton = findViewById(R.id.save_profile_button)
        logoutButton = findViewById(R.id.logout_button)
        deactivateAccountButton = findViewById(R.id.deactivate_account_button)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        setEditMode(false)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.navigation_profile
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navigateToHome(); true
                }
                R.id.navigation_station -> {
                    navigateToStationMap(); true
                }
                else -> false
            }
        }
    }

    private fun loadUserData() {
        val user = dbHelper.getUserByNic(userNic)
        if (user != null) {
            fullNameInput.setText(user["full_name"])
            emailInput.setText(user["email"])
            phoneInput.setText(user["phone"])
        }
    }

    private fun setupClickListeners() {
        editToggleButton.setOnClickListener { toggleEditMode() }

        saveProfileButton.setOnClickListener {
            if (validateForm()) {
                updateUserProfile()
            }
        }

        logoutButton.setOnClickListener { logout() }

        // 🔹 NEW: Deactivate account confirmation + API call
        deactivateAccountButton.setOnClickListener {
            showDeactivateConfirmationDialog()
        }
    }

    private fun validateForm(): Boolean {
        val name = fullNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()

        fullNameInput.error = null
        emailInput.error = null
        phoneInput.error = null

        if (name.isEmpty()) {
            fullNameInput.error = "Full name required"; return false
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Invalid email"; return false
        }
        if (phone.isEmpty() || phone.length < 10) {
            phoneInput.error = "Invalid phone"; return false
        }
        return true
    }

    private fun updateUserProfile() {
        val username = fullNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()

        saveProfileButton.isEnabled = false
        saveProfileButton.text = "Saving..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()

                val jsonBody = JSONObject().apply {
                    put("username", username)
                    put("email", email)
                    put("phone", phone)
                    put("nic", userNic)
                }

                val requestBody = jsonBody.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/api/users/$userNic") // backend PUT endpoint
                    .put(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        dbHelper.insertUser(
                            userNic,
                            username,
                            email,
                            phone,
                            "EvOwner",
                            System.currentTimeMillis().toString()
                        )

                        Toast.makeText(
                            this@ProfileActivity,
                            "Profile updated successfully!",
                            Toast.LENGTH_LONG
                        ).show()

                        setEditMode(false)
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Failed: ${body.ifEmpty { "Unknown error" }}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    saveProfileButton.isEnabled = true
                    saveProfileButton.text = "Save Changes"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    saveProfileButton.isEnabled = true
                    saveProfileButton.text = "Save Changes"
                    Toast.makeText(
                        this@ProfileActivity,
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // 🔹 NEW FUNCTION — show dialog before deactivating
    private fun showDeactivateConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Deactivate Account")
            .setMessage("Are you sure you want to deactivate your account? You will be logged out and cannot log in until reactivated by admin.")
            .setPositiveButton("Deactivate") { dialog, _ ->
                deactivateAccount()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    // 🔹 NEW FUNCTION — call backend PATCH API
    private fun deactivateAccount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()

                val request = Request.Builder()
                    // 👇 For Android emulator use 10.0.2.2 instead of localhost
                    .url("http://10.0.2.2:8080/api/users/$userNic/deactivate")
                    .patch("".toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Account deactivated successfully.",
                            Toast.LENGTH_LONG
                        ).show()

                        // ✅ Clear session
                        getSharedPreferences("EV_PREFS", Context.MODE_PRIVATE)
                            .edit()
                            .remove("USER_NIC")
                            .apply()

                        // ✅ Redirect to Login
                        navigateToLogin()
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Failed: ${responseBody.ifEmpty { "Unknown error" }}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        setEditMode(isEditMode)
    }

    private fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        fullNameInput.isEnabled = enabled
        emailInput.isEnabled = enabled
        phoneInput.isEnabled = enabled

        if (enabled) {
            editToggleButton.text = "Cancel"
            saveProfileButton.visibility = android.view.View.VISIBLE
        } else {
            editToggleButton.text = "Edit"
            saveProfileButton.visibility = android.view.View.GONE
            loadUserData()
        }
    }

    private fun logout() {
        getSharedPreferences("EV_PREFS", Context.MODE_PRIVATE)
            .edit()
            .remove("USER_NIC")
            .apply()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToStationMap() {
        startActivity(Intent(this, StationMapActivity::class.java))
        finish()
    }
}
