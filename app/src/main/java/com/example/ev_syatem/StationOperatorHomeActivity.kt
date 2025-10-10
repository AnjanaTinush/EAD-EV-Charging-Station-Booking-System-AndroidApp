package com.example.ev_syatem

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ev_syatem.database.DatabaseHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class StationOperatorHomeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContentView(R.layout.activity_station_operator_home)

        try {
            dbHelper = DatabaseHelper(this)
        } catch (e: Exception) {
            Toast.makeText(this, "Database error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            return
        }

        setupHeader()
        setupQuickControls()
        setupBottomNav()
    }

    private fun setupHeader() {
        try {
            val operatorNameView = findViewById<TextView>(R.id.operator_name)
            val user = dbHelper.getLatestUser()
            val displayName = user?.get("username") ?: user?.get("full_name") ?: "Station Operator"
            operatorNameView.text = displayName

            // Profile icon click → open profile
            val profileIcon = findViewById<ImageView>(R.id.profile_icon)
            profileIcon?.setOnClickListener {
                startActivity(Intent(this, StationOperatorProfileActivity::class.java))
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Header setup failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupQuickControls() {
        try {
            findViewById<CardView>(R.id.manage_station_button)?.setOnClickListener {
                startActivity(Intent(this, StationMapActivity::class.java))
            }

            findViewById<CardView>(R.id.view_requests_button)?.setOnClickListener {
                startActivity(Intent(this, BookingActivity::class.java))
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Quick controls failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNav() {
        try {
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_operator)
            bottomNav.selectedItemId = R.id.navigation_home_operator

            bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home_operator -> true
                    R.id.navigation_station_operator -> {
                        startActivity(Intent(this, StationMapActivity::class.java))
                        true
                    }
                    R.id.navigation_profile_operator -> {
                        startActivity(Intent(this, StationOperatorProfileActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Navigation setup failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
