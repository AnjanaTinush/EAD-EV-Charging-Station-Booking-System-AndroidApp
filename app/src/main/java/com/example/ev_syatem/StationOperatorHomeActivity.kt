package com.example.ev_syatem

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class StationOperatorHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContentView(R.layout.activity_station_operator_home)

        // ✅ Find the button and set its click listener
        val scanQrButton: CardView = findViewById(R.id.scan_qr_button)
        scanQrButton.setOnClickListener {
            // ✅ Navigate to the new QrScannerActivity
            startActivity(Intent(this, QrScannerActivity::class.java))
        }
    }
}
