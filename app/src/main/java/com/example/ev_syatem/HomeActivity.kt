package com.example.ev_syatem

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ev_syatem.repository.ReservationRepository
import com.example.ev_syatem.repository.UserRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {
    private lateinit var userNameText: TextView
    private lateinit var pendingCountText: TextView
    private lateinit var approvedCountText: TextView
    private lateinit var bookStationButton: MaterialButton
    private lateinit var myReservationsButton: MaterialButton
    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var userRepository: UserRepository
    private lateinit var reservationRepository: ReservationRepository
    private var userNic: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar color to match background
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = getColor(R.color.primary_green)

        // Make status bar content light (for dark background)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.isAppearanceLightStatusBars = false
        }

        setContentView(R.layout.activity_home)

        // Get user NIC from SharedPreferences or Intent
        userNic = getSharedPreferences("EV_PREFS", Context.MODE_PRIVATE)
            .getString("USER_NIC", "") ?: ""

        if (userNic.isEmpty()) {
            // No user logged in, redirect to login
            navigateToLogin()
            return
        }

        // Initialize repositories
        userRepository = UserRepository(this)
        reservationRepository = ReservationRepository(this)

        initializeViews()
        setupToolbar()
        setupBottomNavigation()
        loadUserData()
        loadReservationCounts()
        setupClickListeners()
    }

    private fun initializeViews() {
        try {
            toolbar = findViewById(R.id.toolbar)
            userNameText = findViewById(R.id.user_name_text)
            pendingCountText = findViewById(R.id.pending_count_text)
            approvedCountText = findViewById(R.id.approved_count_text)
            bookStationButton = findViewById(R.id.book_station_button)
            myReservationsButton = findViewById(R.id.my_reservations_button)
            bottomNavigation = findViewById(R.id.bottom_navigation)
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing views: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(toolbar)
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up toolbar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.navigation_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Already on home, do nothing
                    true
                }
                R.id.navigation_booking -> {
                    Toast.makeText(this, "Booking feature coming soon!", Toast.LENGTH_SHORT).show()
                    false
                }
                R.id.navigation_station -> {
                    Toast.makeText(this, "Stations feature coming soon!", Toast.LENGTH_SHORT).show()
                    false
                }
                R.id.navigation_profile -> {
                    navigateToProfile()
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadUserData() {
        try {
            val user = userRepository.getUserByNic(userNic)
            if (user != null) {
                userNameText.text = user.fullName
            } else {
                userNameText.text = "Guest User"
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
            userNameText.text = "User"
        }
    }

    private fun loadReservationCounts() {
        try {
            // Get pending reservations count
            val pendingCount = reservationRepository.getPendingReservationsCount(userNic)
            pendingCountText.text = pendingCount.toString()

            // Get approved future reservations count
            val approvedCount = reservationRepository.getApprovedFutureReservationsCount(userNic)
            approvedCountText.text = approvedCount.toString()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading reservations: ${e.message}", Toast.LENGTH_SHORT).show()
            pendingCountText.text = "0"
            approvedCountText.text = "0"
        }
    }

    private fun setupClickListeners() {
        bookStationButton.setOnClickListener {
            // TODO: Navigate to book station screen
            Toast.makeText(this, "Book Station feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        myReservationsButton.setOnClickListener {
            // TODO: Navigate to my reservations screen
            Toast.makeText(this, "My Reservations feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        // Clear user session
        getSharedPreferences("EV_PREFS", Context.MODE_PRIVATE)
            .edit()
            .remove("USER_NIC")
            .apply()

        // Navigate to login
        navigateToLogin()
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
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Refresh counts when returning to this screen
        if (userNic.isNotEmpty()) {
            loadReservationCounts()
        }
    }
}