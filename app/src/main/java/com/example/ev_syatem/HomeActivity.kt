package com.example.ev_syatem

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ev_syatem.database.DatabaseHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var userNameText: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContentView(R.layout.activity_home)

        userNameText = findViewById(R.id.user_name_text)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        dbHelper = DatabaseHelper(this)

        loadUserData()
        setupBottomNavigation()
        setupClickListeners() // ✅ Call the new function
    }

    private fun loadUserData() {
        val user = dbHelper.getLatestUser()
        if (user != null) {
            // Assuming the key for the user's name is "username" from the login response
            userNameText.text = "Welcome, ${user["username"]}"
        } else {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.navigation_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_profile -> {
                    navigateToProfile()
                    true
                }
                // ✅ Add navigation for booking history
                R.id.navigation_booking -> {
                    navigateToMyReservations()
                    true
                }
                else -> false
            }
        }
    }

    // ✅ NEW: Centralize click listeners here
    private fun setupClickListeners() {
        val bookStationButton: LinearLayout = findViewById(R.id.book_station_button)
        bookStationButton.setOnClickListener {
            navigateToBooking()
        }

        // ✅ Add listener for the new "My Reservations" button
        val myReservationsButton: LinearLayout = findViewById(R.id.my_reservations_button)
        myReservationsButton.setOnClickListener {
            navigateToMyReservations()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToBooking() {
        val intent = Intent(this, BookingActivity::class.java)
        startActivity(intent)
    }

    // ✅ NEW: Navigation function for My Reservations
    private fun navigateToMyReservations() {
        val intent = Intent(this, BookingsActivity::class.java)
        startActivity(intent)
    }
}
