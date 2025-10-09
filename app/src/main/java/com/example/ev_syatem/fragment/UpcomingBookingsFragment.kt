package com.example.ev_syatem.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
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
import com.example.ev_syatem.adapter.BookingAdapter
import com.example.ev_syatem.data.Booking
import com.example.ev_syatem.database.DatabaseHelper
import com.example.ev_syatem.repository.BookingRepository
import java.util.*

class UpcomingBookingsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var bookingAdapter: BookingAdapter
    private val bookingRepository = BookingRepository()
    private val bookings = mutableListOf<Booking>()
    private lateinit var dbHelper: DatabaseHelper // ✅ Add DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dbHelper = DatabaseHelper(requireContext()) // ✅ Initialize it
        return inflater.inflate(R.layout.fragment_upcoming_bookings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        emptyState = view.findViewById(R.id.empty_state)
        progressBar = view.findViewById(R.id.progress_bar)

        setupRecyclerView()
        setupSwipeRefresh()
        loadUpcomingBookings()
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(
            bookings,
            onModifyClick = { booking -> showModifyDialog(booking) },
            onCancelClick = { booking -> showCancelDialog(booking) }
        )
        recyclerView.adapter = bookingAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(requireContext().getColor(R.color.primary_green))
        swipeRefresh.setOnRefreshListener { loadUpcomingBookings() }
    }

    private fun loadUpcomingBookings() {
        showLoading(true)

        // ✅ Get owner NIC from local database
        val user = dbHelper.getLatestUser()
        val ownerNIC = user?.get("nic")

        if (ownerNIC == null) {
            Toast.makeText(context, "Error: User not logged in.", Toast.LENGTH_LONG).show()
            showLoading(false)
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

    // ... rest of your functions (showModifyDialog, cancelBooking, etc.) remain the same
    private fun showModifyDialog(booking: Booking) {
        val calendar = Calendar.getInstance()

        // Show date picker
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                // Show time picker after date selection
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        val newDateTime = String.format(
                            Locale.US, // Use Locale for consistency
                            "%04d-%02d-%02dT%02d:%02d:00Z",
                            year, month + 1, day, hour, minute
                        )
                        updateBooking(booking.id!!, newDateTime) // Use non-null assertion
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun updateBooking(bookingId: String, newDateTime: String) {
        progressBar.visibility = View.VISIBLE

        bookingRepository.updateBooking(bookingId, newDateTime) { success, message ->
            activity?.runOnUiThread {
                progressBar.visibility = View.GONE

                if (success) {
                    Toast.makeText(context, "✓ Booking updated successfully", Toast.LENGTH_SHORT).show()
                    loadUpcomingBookings()
                } else {
                    Toast.makeText(context, "✗ Failed to update: $message", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showCancelDialog(booking: Booking) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                cancelBooking(booking.id!!) // Use non-null assertion
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelBooking(bookingId: String) {
        progressBar.visibility = View.VISIBLE

        bookingRepository.cancelBooking(bookingId, "User cancelled") { success, message ->
            activity?.runOnUiThread {
                progressBar.visibility = View.GONE

                if (success) {
                    Toast.makeText(context, "✓ Booking cancelled", Toast.LENGTH_SHORT).show()
                    loadUpcomingBookings()
                } else {
                    Toast.makeText(context, "✗ Failed to cancel: $message", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
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

