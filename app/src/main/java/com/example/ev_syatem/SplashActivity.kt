package com.example.ev_syatem

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
// Removed the bad imports from this section
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ev_syatem.database.DatabaseHelper // ✅ Import DatabaseHelper

class SplashActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper // ✅ Add DatabaseHelper instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        setContentView(R.layout.activity_splash)

        dbHelper = DatabaseHelper(this) // ✅ Initialize DatabaseHelper

        // Delay to show splash screen, then check login status
        Handler(Looper.getMainLooper()).postDelayed({
            // ✅ Check if a user is already logged in
            if (dbHelper.isUserLoggedIn()) {
                // User is logged in, go to HomeActivity
                navigateTo(HomeActivity::class.java)
            } else {
                // No user logged in, go to LoginActivity
                navigateTo(LoginActivity::class.java)
            }
        }, 2000) // 2-second delay
    }

    // ✅ Helper function for navigation
    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Corrected the getColor method to be compatible with older and newer APIs
        window.statusBarColor = getColor(R.color.background_primary)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.isAppearanceLightStatusBars = true
        }
    }
}
