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
// ✅ Import BookingRepository
import com.example.ev_syatem.repository.BookingRepository
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var userNameText: TextView
    // ✅ Add TextViews for the counts
    private lateinit var pendingCountText: TextView
    private lateinit var approvedCountText: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var dbHelper: DatabaseHelper
    // ✅ Add an instance of the repository
    private val bookingRepository = BookingRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContentView(R.layout.activity_home)

        // ✅ Initialize all views
        userNameText = findViewById(R.id.user_name_text)
        pendingCountText = findViewById(R.id.pending_count_text)
        approvedCountText = findViewById(R.id.approved_count_text)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        dbHelper = DatabaseHelper(this)

        loadUserData()
        setupBottomNavigation()
        setupClickListeners()
    }

    // Refresh data every time the user comes back to the home screen
    override fun onResume() {
        super.onResume()
        loadBookingCounts()
    }

    private fun loadUserData() {
        val user = dbHelper.getLatestUser()
        if (user != null) {
            userNameText.text = "Welcome, ${user["username"]}"
        } else {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    // ✅ This function fetches the bookings and updates the UI
    private fun loadBookingCounts() {
        val user = dbHelper.getLatestUser()
        val ownerNIC = user?.get("nic")

        if (ownerNIC == null) {
            // Can't fetch data without a user, so set counts to 0
            pendingCountText.text = "0"
            approvedCountText.text = "0"
            return
        }

        // ✅ FIX: Use the correct, existing getUpcomingBookings function
        bookingRepository.getUpcomingBookings(ownerNIC) { bookings ->
            // Filter the list to get counts for each status
            val pendingCount = bookings.count { it.status.equals("Pending", ignoreCase = true) }
            val approvedCount = bookings.count { it.status.equals("Approved", ignoreCase = true) }

            // Update the UI on the main thread
            runOnUiThread {
                pendingCountText.text = pendingCount.toString()
                approvedCountText.text = approvedCount.toString()
            }
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
                R.id.navigation_booking -> {
                    navigateToMyReservations()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        val bookStationButton: LinearLayout = findViewById(R.id.book_station_button)
        bookStationButton.setOnClickListener {
            navigateToBooking()
        }

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

    private fun navigateToMyReservations() {
        val intent = Intent(this, BookingsActivity::class.java)
        startActivity(intent)
    }
}
