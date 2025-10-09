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
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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

        setupRecyclerView()
        setupSwipeRefresh()
    }

    override fun onResume() {
        super.onResume()
        // Load bookings when the fragment becomes visible
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

        // ✅ Get the logged-in user from the local database
        val user = dbHelper.getLatestUser()
        val ownerNIC = user?.get("nic")

        // ✅ Check if NIC exists before making the call
        if (ownerNIC == null) {
            Toast.makeText(context, "Could not identify user. Please log in again.", Toast.LENGTH_LONG).show()
            showLoading(false)
            updateEmptyState() // Show empty state if no user
            return
        }

        // ✅ Pass the owner's NIC to the repository function
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
                        // Use the booking ID which should be a String
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
                    loadUpcomingBookings() // Refresh list on success
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
                    loadUpcomingBookings() // Refresh list on success
                } else {
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        if(isLoading) emptyState.visibility = View.GONE
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
