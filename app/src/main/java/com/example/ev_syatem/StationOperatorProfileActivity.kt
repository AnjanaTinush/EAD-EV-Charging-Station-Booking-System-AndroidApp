package com.example.ev_syatem

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.example.ev_syatem.database.DatabaseHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class StationOperatorProfileActivity : ProfileActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_operator_profile)

        dbHelper = DatabaseHelper(this)
        loadOperatorInfo()
        setupBottomNav()
    }

    private fun loadOperatorInfo() {
        val user = dbHelper.getLatestUser()

        findViewById<TextView>(R.id.operator_name_profile).text =
            user?.get("username") ?: "Station Operator"
        findViewById<TextView>(R.id.operator_email).text =
            "Email: ${user?.get("email") ?: "N/A"}"
        findViewById<TextView>(R.id.operator_phone).text =
            "Phone: ${user?.get("phone") ?: "N/A"}"
        findViewById<TextView>(R.id.operator_nic).text =
            "NIC: ${user?.get("nic") ?: "N/A"}"
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_operator)
        bottomNav.selectedItemId = R.id.navigation_profile_operator

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home_operator -> {
                    startActivity(Intent(this, StationOperatorHomeActivity::class.java))
                    true
                }
                R.id.navigation_station_operator -> {
                    startActivity(Intent(this, StationMapActivity::class.java))
                    true
                }
                R.id.navigation_profile_operator -> true
                else -> false
            }
        }
    }
}
