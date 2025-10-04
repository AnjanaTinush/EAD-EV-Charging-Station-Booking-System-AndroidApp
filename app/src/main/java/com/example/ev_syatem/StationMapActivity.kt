package com.example.ev_syatem

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class StationMapActivity : AppCompatActivity(), LocationListener {

    private lateinit var mapWebView: WebView
    private lateinit var backButton: ImageView
    private lateinit var centerLocationButton: ImageView
    private lateinit var locationStatusText: TextView
    private lateinit var loadingProgress: ProgressBar
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var locationManager: LocationManager
    private var currentLatitude: Double = 6.9271 // Default to Colombo, Sri Lanka
    private var currentLongitude: Double = 79.8612
    private var hasLocation: Boolean = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar to transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.isAppearanceLightStatusBars = true
        }

        setContentView(R.layout.activity_station_map)

        initializeViews()
        setupWebView()
        setupClickListeners()
        setupBottomNavigation()
        loadLeafletMap() // Load map immediately with default location
        checkLocationPermission()
    }

    private fun initializeViews() {
        mapWebView = findViewById(R.id.map_webview)
        backButton = findViewById(R.id.back_button)
        centerLocationButton = findViewById(R.id.center_location_button)
        locationStatusText = findViewById(R.id.location_status_text)
        loadingProgress = findViewById(R.id.loading_progress)
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun setupWebView() {
        mapWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        mapWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                loadingProgress.visibility = View.GONE
                if (hasLocation) {
                    updateMapLocation()
                }
            }
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        centerLocationButton.setOnClickListener {
            if (hasLocation) {
                updateMapLocation()
                Toast.makeText(this, "Centered on your location", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.navigation_station

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
                    // Already on station map
                    true
                }
                R.id.navigation_profile -> {
                    navigateToProfile()
                    true
                }
                else -> false
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startLocationUpdates() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            // Try to get last known location first
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val lastKnownLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                if (lastKnownLocation != null) {
                    onLocationChanged(lastKnownLocation)
                }

                // Request location updates
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000L,
                    10f,
                    this
                )

                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000L,
                    10f,
                    this
                )
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
            loadMapWithDefaultLocation()
        }
    }

    override fun onLocationChanged(location: Location) {
        currentLatitude = location.latitude
        currentLongitude = location.longitude
        hasLocation = true

        locationStatusText.text = "Location: ${String.format("%.4f", currentLatitude)}, ${
            String.format("%.4f", currentLongitude)
        }"

        if (mapWebView.url == null) {
            loadLeafletMap()
        } else {
            updateMapLocation()
        }
    }

    private fun loadLeafletMap() {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    body { margin: 0; padding: 0; }
                    #map {
                        height: 100vh;
                        width: 100vw;
                    }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map').setView([$currentLatitude, $currentLongitude], 15);

                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '&copy; OpenStreetMap contributors',
                        maxZoom: 19
                    }).addTo(map);

                    var currentLocationMarker = L.marker([$currentLatitude, $currentLongitude], {
                        title: 'Your Location'
                    }).addTo(map);

                    currentLocationMarker.bindPopup('<b>Your Current Location</b>').openPopup();

                    // Custom icon for current location
                    var blueIcon = L.divIcon({
                        className: 'custom-div-icon',
                        html: "<div style='background-color:#4285F4;width:20px;height:20px;border-radius:50%;border:3px solid white;box-shadow:0 2px 6px rgba(0,0,0,0.3);'></div>",
                        iconSize: [26, 26],
                        iconAnchor: [13, 13]
                    });

                    currentLocationMarker.setIcon(blueIcon);

                    // Sample charging stations (you can replace these with real data)
                    var stations = [
                        {lat: $currentLatitude + 0.01, lng: $currentLongitude + 0.01, name: "Station A"},
                        {lat: $currentLatitude - 0.01, lng: $currentLongitude + 0.01, name: "Station B"},
                        {lat: $currentLatitude + 0.01, lng: $currentLongitude - 0.01, name: "Station C"}
                    ];

                    stations.forEach(function(station) {
                        var greenIcon = L.divIcon({
                            className: 'custom-div-icon',
                            html: "<div style='background-color:#34A853;width:30px;height:30px;border-radius:50%;border:3px solid white;box-shadow:0 2px 6px rgba(0,0,0,0.3);display:flex;align-items:center;justify-content:center;'><span style='color:white;font-weight:bold;'>⚡</span></div>",
                            iconSize: [36, 36],
                            iconAnchor: [18, 18]
                        });

                        L.marker([station.lat, station.lng], {icon: greenIcon})
                            .addTo(map)
                            .bindPopup('<b>' + station.name + '</b><br>EV Charging Station');
                    });

                    // Function to update location
                    function updateLocation(lat, lng) {
                        map.setView([lat, lng], 15);
                        currentLocationMarker.setLatLng([lat, lng]);
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        mapWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    private fun updateMapLocation() {
        mapWebView.evaluateJavascript(
            "updateLocation($currentLatitude, $currentLongitude);",
            null
        )
    }

    private fun loadMapWithDefaultLocation() {
        hasLocation = false
        locationStatusText.text = "Using default location (Colombo)"
        loadLeafletMap()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied. Using default location.",
                    Toast.LENGTH_LONG
                ).show()
                loadMapWithDefaultLocation()
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            locationManager.removeUpdates(this)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
