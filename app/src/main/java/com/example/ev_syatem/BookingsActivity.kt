package com.example.ev_syatem

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.ev_syatem.fragment.BookingHistoryFragment
import com.example.ev_syatem.fragment.UpcomingBookingsFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class BookingsActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var btnBack: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookings)

        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        btnBack = findViewById(R.id.btn_back)

        setupViewPager()
        setupBackButton()
    }

    private fun setupViewPager() {
        val adapter = BookingsPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Upcoming"
                1 -> "History"
                else -> ""
            }
        }.attach()
    }

    private fun setupBackButton() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private inner class BookingsPagerAdapter(activity: FragmentActivity) :
        FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> UpcomingBookingsFragment()
                1 -> BookingHistoryFragment()
                else -> UpcomingBookingsFragment()
            }
        }
    }
}