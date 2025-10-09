package com.example.ev_syatem.fragment

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
        // Using the same layout as UpcomingBookingsFragment
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
        loadBookingHistory()
    }

    private fun setupRecyclerView() {
        // Past bookings don’t allow modify or cancel
        bookingAdapter = BookingAdapter(bookings, onModifyClick = null, onCancelClick = null)
        recyclerView.adapter = bookingAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(requireContext().getColor(R.color.primary_green))
        swipeRefresh.setOnRefreshListener { loadBookingHistory() }
    }

    private fun loadBookingHistory() {
        showLoading(true)

        // Get logged-in user NIC
        val user = dbHelper.getLatestUser()
        val ownerNIC = user?.get("nic")

        if (ownerNIC == null) {
            Toast.makeText(context, "Error: User not logged in.", Toast.LENGTH_LONG).show()
            showLoading(false)
            return
        }

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
