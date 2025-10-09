package com.example.ev_syatem.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.ev_syatem.BookingDetailsActivity
import com.example.ev_syatem.R
import com.example.ev_syatem.adapter.BookingAdapter
import com.example.ev_syatem.data.Booking
import com.example.ev_syatem.database.DatabaseHelper
import com.example.ev_syatem.repository.BookingRepository

class BookingHistoryFragment : Fragment() {

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
        // Load history when the fragment becomes visible
        loadBookingHistory()
    }

    private fun setupRecyclerView() {
        // History items are not modifiable
        bookingAdapter = BookingAdapter(
            bookings,
            // ✅ FIX: Add the onItemClick listener to handle navigation
            onItemClick = { booking ->
                val intent = Intent(requireContext(), BookingDetailsActivity::class.java).apply {
                    putExtra("STATION_ID", booking.stationId)
                    putExtra("RESERVATION_TIME", booking.reservationTime)
                    putExtra("STATUS", booking.status)
                    putExtra("QR_CODE", booking.qrCodeBase64)
                }
                startActivity(intent)
            },
            onModifyClick = null,
            onCancelClick = null
        )
        recyclerView.adapter = bookingAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(requireContext().getColor(R.color.primary_green))
        swipeRefresh.setOnRefreshListener { loadBookingHistory() }
    }

    private fun loadBookingHistory() {
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
        bookingRepository.getBookingHistory(ownerNIC) { fetchedBookings ->
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
