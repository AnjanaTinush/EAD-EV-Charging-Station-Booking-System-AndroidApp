package com.example.ev_syatem.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
// ✅ FIX: Corrected the import statement
import android.content.Intent
import android.os.Bundle
// ✅ FIX: Removed duplicate imports that were here
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.ev_syatem.R
// ✅ Import BookingDetailsActivity
import com.example.ev_syatem.BookingDetailsActivity
import com.example.ev_syatem.adapter.BookingAdapter
import com.example.ev_syatem.data.Booking
import com.example.ev_syatem.database.DatabaseHelper
import com.example.ev_syatem.repository.BookingRepository
import java.util.*

class UpcomingBookingsFragment : Fragment() {

    // ... (Your existing properties are fine)
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var bookingAdapter: BookingAdapter
    private val bookingRepository = BookingRepository()
    private val bookings = mutableListOf<Booking>()
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        dbHelper = DatabaseHelper(requireContext())
        return inflater.inflate(R.layout.fragment_upcoming_bookings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        emptyState = view.findViewById(R.id.empty_state)
        progressBar = view.findViewById(R.id.progress_bar)

        setupRecyclerView() // This is where we will make the change
        setupSwipeRefresh()
    }

    // ... (onResume, loadUpcomingBookings, etc. remain the same)
    override fun onResume() {
        super.onResume()
        loadUpcomingBookings()
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(
            bookings,
            // ✅ 3. Implement the onItemClick logic
            onItemClick = { booking ->
                // Create an intent to navigate to the details activity
                val intent = Intent(requireContext(), BookingDetailsActivity::class.java).apply {
                    // Pass all the relevant data from the clicked booking
                    putExtra("STATION_ID", booking.stationId)
                    putExtra("RESERVATION_TIME", booking.reservationTime)
                    putExtra("STATUS", booking.status)
                    putExtra("QR_CODE", booking.qrCodeBase64)
                }
                // Start the activity
                startActivity(intent)
            },
            onModifyClick = { booking -> showModifyDialog(booking) },
            onCancelClick = { booking -> showCancelDialog(booking) }
        )
        recyclerView.adapter = bookingAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    // ... (All other functions in the fragment remain the same)
    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(requireContext().getColor(R.color.primary_green))
        swipeRefresh.setOnRefreshListener { loadUpcomingBookings() }
    }

    private fun loadUpcomingBookings() {
        showLoading(true)
        val user = dbHelper.getLatestUser()
        val ownerNIC = user?.get("nic")
        if (ownerNIC == null) {
            Toast.makeText(context, "Could not identify user. Please log in again.", Toast.LENGTH_LONG).show()
            showLoading(false)
            updateEmptyState()
            return
        }
        bookingRepository.getUpcomingBookings(ownerNIC) { fetchedBookings ->
            activity?.runOnUiThread {
                showLoading(false)
                swipeRefresh.isRefreshing = false
                bookings.clear()
                bookings.addAll(fetchedBookings)
                bookingAdapter.notifyDataSetChanged()
                updateEmptyState()
            }
        }
    }

    private fun showModifyDialog(booking: Booking) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        val newDateTime = String.format(
                            Locale.US,
                            "%04d-%02d-%02dT%02d:%02d:00Z",
                            year, month + 1, day, hour, minute
                        )
                        updateBooking(booking.id, newDateTime)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun updateBooking(bookingId: String, newDateTime: String) {
        showLoading(true)
        bookingRepository.updateBooking(bookingId, newDateTime) { success, message ->
            activity?.runOnUiThread {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                if (success) {
                    loadUpcomingBookings()
                } else {
                    showLoading(false)
                }
            }
        }
    }

    private fun showCancelDialog(booking: Booking) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                cancelBooking(booking.id)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelBooking(bookingId: String) {
        showLoading(true)
        bookingRepository.cancelBooking(bookingId, "User cancelled") { success, message ->
            activity?.runOnUiThread {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                if (success) {
                    loadUpcomingBookings()
                } else {
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        if (isLoading) emptyState.visibility = View.GONE
    }

    private fun updateEmptyState() {
        if (bookings.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
