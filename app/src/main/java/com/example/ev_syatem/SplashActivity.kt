package com.example.ev_syatem

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide system bars for immersive splash screen
        hideSystemBars()

        setContentView(R.layout.activity_splash)

        // Delay 2 seconds before moving to LoginActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close SplashActivity so user cannot go back to it
        }, 2000) // 2000 ms = 2 seconds
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set status bar color to match background
        window.statusBarColor = getColor(R.color.background_primary)

        // Make status bar content dark (for light background)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.isAppearanceLightStatusBars = true
        }
    }
}
