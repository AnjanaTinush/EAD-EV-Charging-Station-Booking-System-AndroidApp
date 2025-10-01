package com.example.ev_syatem

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ev_syatem.repository.UserRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

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

    private lateinit var userRepository: UserRepository
    private var userNic: String = ""
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar to transparent for modern look
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.isAppearanceLightStatusBars = true
        }

        setContentView(R.layout.activity_profile)

        // Get user NIC from SharedPreferences
        userNic = getSharedPreferences("EV_PREFS", Context.MODE_PRIVATE)
            .getString("USER_NIC", "") ?: ""

        if (userNic.isEmpty()) {
            navigateToLogin()
            return
        }

        // Initialize repository
        userRepository = UserRepository(this)

        initializeViews()
        setupBottomNavigation()
        loadUserData()
        setupClickListeners()
    }

    private fun initializeViews() {
        profileNameText = findViewById(R.id.profile_name_text)
        profileNicText = findViewById(R.id.profile_nic_text)
        fullNameInput = findViewById(R.id.fullNameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        editToggleButton = findViewById(R.id.edit_toggle_button)
        saveProfileButton = findViewById(R.id.save_profile_button)
        logoutButton = findViewById(R.id.logout_button)
        deactivateAccountButton = findViewById(R.id.deactivate_account_button)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Initially disable editing
        setEditMode(false)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.navigation_profile

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navigateToHome()
                    true
                }
                R.id.navigation_booking -> {
                    Toast.makeText(this, "Booking feature coming soon!", Toast.LENGTH_SHORT).show()
                    false
                }
                R.id.navigation_station -> {
                    navigateToStationMap()
                    true
                }
                R.id.navigation_profile -> {
                    // Already on profile
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserData() {
        try {
            val user = userRepository.getUserByNic(userNic)
            if (user != null) {
                profileNameText.text = user.fullName
                profileNicText.text = user.nic
                fullNameInput.setText(user.fullName)
                emailInput.setText(user.email)
                phoneInput.setText(user.phone)
            } else {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        editToggleButton.setOnClickListener {
            toggleEditMode()
        }

        saveProfileButton.setOnClickListener {
            if (validateForm()) {
                saveProfile()
            }
        }

        logoutButton.setOnClickListener {
            logout()
        }

        deactivateAccountButton.setOnClickListener {
            showDeactivateConfirmationDialog()
        }
    }

    private fun logout() {
        // Clear user session
        getSharedPreferences("EV_PREFS", Context.MODE_PRIVATE)
            .edit()
            .remove("USER_NIC")
            .apply()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to login
        navigateToLogin()
    }

    private fun showDeactivateConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Deactivate Account")
            .setMessage("Are you sure you want to deactivate your account? You will be logged out and won't be able to login until your account is reactivated by an administrator.")
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

    private fun deactivateAccount() {
        try {
            // Update user's isActivate status to false
            val result = userRepository.updateUserActivation(userNic, false)

            if (result > 0) {
                Toast.makeText(this, "Account deactivated successfully", Toast.LENGTH_LONG).show()

                // Clear user session
                getSharedPreferences("EV_PREFS", MODE_PRIVATE)
                    .edit()
                    .remove("USER_NIC")
                    .apply()

                // Navigate to login
                navigateToLogin()
            } else {
                Toast.makeText(this, "Failed to deactivate account. Please try again.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        setEditMode(isEditMode)
    }

    private fun setEditMode(enabled: Boolean) {
        isEditMode = enabled

        // Enable/disable input fields
        fullNameInput.isEnabled = enabled
        emailInput.isEnabled = enabled
        phoneInput.isEnabled = enabled

        // Change button appearance and visibility
        if (enabled) {
            editToggleButton.text = "Cancel"
            editToggleButton.setIconResource(android.R.drawable.ic_menu_close_clear_cancel)
            saveProfileButton.visibility = android.view.View.VISIBLE
        } else {
            editToggleButton.text = "Edit"
            editToggleButton.setIconResource(R.drawable.ic_edit)
            saveProfileButton.visibility = android.view.View.GONE
            // Reload data to reset any unsaved changes
            loadUserData()
        }
    }

    private fun validateForm(): Boolean {
        val fullName = fullNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()

        // Reset errors
        fullNameInput.error = null
        emailInput.error = null
        phoneInput.error = null

        var isValid = true

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

        return isValid
    }

    private fun saveProfile() {
        val fullName = fullNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()

        // Disable save button during save
        saveProfileButton.isEnabled = false
        saveProfileButton.text = "Saving..."

        try {
            // Check if email is being changed and if it already exists for another user
            val currentUser = userRepository.getUserByNic(userNic)
            if (currentUser != null && email != currentUser.email) {
                if (userRepository.isEmailExists(email)) {
                    emailInput.error = "This email is already in use"
                    Toast.makeText(this, "Email already exists", Toast.LENGTH_LONG).show()
                    // Re-enable button
                    saveProfileButton.isEnabled = true
                    saveProfileButton.text = "Save Changes"
                    return
                }
            }

            // Update user in database
            val updatedUser = currentUser?.copy(
                fullName = fullName,
                email = email,
                phone = phone
            )

            if (updatedUser != null) {
                val result = userRepository.updateUser(updatedUser)
                if (result > 0) {
                    // Success feedback
                    saveProfileButton.text = "✓ Saved!"
                    Toast.makeText(this, "✓ Profile updated successfully!", Toast.LENGTH_LONG).show()

                    // Exit edit mode after delay
                    saveProfileButton.postDelayed({
                        setEditMode(false)
                        saveProfileButton.isEnabled = true
                        saveProfileButton.text = "Save Changes"
                    }, 1500)
                } else {
                    Toast.makeText(this, "Failed to update profile. Please try again.", Toast.LENGTH_LONG).show()
                    saveProfileButton.isEnabled = true
                    saveProfileButton.text = "Save Changes"
                }
            } else {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                saveProfileButton.isEnabled = true
                saveProfileButton.text = "Save Changes"
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            saveProfileButton.isEnabled = true
            saveProfileButton.text = "Save Changes"
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToStationMap() {
        val intent = Intent(this, StationMapActivity::class.java)
        startActivity(intent)
        finish()
    }
}
