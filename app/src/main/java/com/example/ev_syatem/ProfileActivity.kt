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

open class ProfileActivity : AppCompatActivity() {

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
    private var isEditMode: Boolean = false

    // SharedPreferences for lightweight session flags
    private val prefs by lazy { getSharedPreferences("auth", Context.MODE_PRIVATE) }

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

            // prime SharedPreferences if not already set
            prefs.edit()
                .putString("user_id", user["server_id"])
                .putString("username", user["username"] ?: user["full_name"])
                .putString("email", user["email"])
                .putString("phone", user["phone"])
                .putString("nic", user["nic"])
                .putString("role", user["role"])
                .putBoolean("is_active", (user["is_active"] ?: "1") == "1")
                .apply()
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
                R.id.navigation_home -> { navigateToHome(); true }
                R.id.navigation_station -> { navigateToStationMap(); true }
                R.id.navigation_booking -> { navigateToBooking(); true }
                else -> false
            }
        }
    }

    private fun loadUserData() {
        val user = dbHelper.getLatestUser()
        if (user != null) {
            fullNameInput.setText(user["full_name"])
            emailInput.setText(user["email"])
            phoneInput.setText(user["phone"])
            profileNameText.text = user["full_name"]
            profileNicText.text = user["nic"]
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

        if (name.isEmpty()) { fullNameInput.error = "Full name required"; return false }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Invalid email"; return false
        }
        if (phone.isEmpty() || phone.length != 10) {
            phoneInput.error = "Phone must be 10 digits"; return false
        }
        return true
    }

    // ===========================
    //       UPDATE PROFILE
    // ===========================
    private fun updateUserProfile() {
        val username = fullNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()

        saveProfileButton.isEnabled = false
        saveProfileButton.text = "Saving..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val user = dbHelper.getLatestUser() ?: return@launch

                // ✅ Use Mongo ID saved in SQLite ("server_id"), not a local "id"
                val userId = user["server_id"] ?: ""
                if (userId.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "User ID missing", Toast.LENGTH_LONG).show()
                        saveProfileButton.isEnabled = true
                        saveProfileButton.text = "Save Changes"
                    }
                    return@launch
                }

                val jsonBody = JSONObject().apply {
                    put("username", username)
                    put("email", email)
                    put("phone", phone)
                    put("nic", userNic) // NIC remains constant
                }

                val requestBody = jsonBody.toString()
                    .toRequestBody("application/json".toMediaType())

                // Use 10.0.2.2 for Android emulator to reach localhost of host
                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/api/users/$userId")
                    .put(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Keep role and isActive from current session
                        val role = user["role"] ?: "EvOwner"
                        val isActive = (user["is_active"] ?: "1") == "1"

                        // ✅ Update SQLite (replace existing session user)
                        dbHelper.clearUsers()
                        dbHelper.insertUser(
                            serverId = userId,
                            username = username,
                            email = email,
                            phone = phone,
                            nic = userNic,
                            role = role,
                            isActive = isActive,
                            createdAt = System.currentTimeMillis().toString()
                        )

                        // ✅ Update SharedPreferences snapshot
                        prefs.edit()
                            .putString("user_id", userId)
                            .putString("username", username)
                            .putString("email", email)
                            .putString("phone", phone)
                            .putString("nic", userNic)
                            .putString("role", role)
                            .putBoolean("is_active", isActive)
                            .apply()

                        Toast.makeText(
                            this@ProfileActivity,
                            "Profile updated successfully!",
                            Toast.LENGTH_LONG
                        ).show()

                        setEditMode(false)
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Update failed: ${body.ifEmpty { "Unknown error" }}",
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

    // ===========================
    //       DEACTIVATE USER
    // ===========================
    private fun showDeactivateConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Deactivate Account")
            .setMessage("Are you sure you want to deactivate your account? You will be logged out and cannot log in until reactivated by an admin.")
            .setPositiveButton("Deactivate") { dialog, _ ->
                deactivateAccount()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }

    private fun deactivateAccount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val user = dbHelper.getLatestUser() ?: return@launch
                val userId = user["server_id"] ?: ""

                if (userId.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "User ID missing", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/api/users/$userId/deactivate")
                    .patch("".toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // mark inactive in SharedPreferences for consistency
                        prefs.edit().putBoolean("is_active", false).apply()
                        Toast.makeText(
                            this@ProfileActivity,
                            "Account deactivated successfully.",
                            Toast.LENGTH_LONG
                        ).show()

                        // ✅ Fully log out the user (clears SQLite + SharedPreferences)
                        logout()
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Deactivation failed: ${responseBody.ifEmpty { "Unknown error" }}",
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

    // ===========================
    //        UI HELPERS
    // ===========================
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
            loadUserData() // discard unsaved edits
        }
    }

    private fun logout() {
        // ✅ Clear local DB
        dbHelper.clearUsers()
        // ✅ Clear SharedPreferences
        prefs.edit().clear().apply()

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

    private fun navigateToBooking() {
        startActivity(Intent(this, BookingActivity::class.java))
    }
}
