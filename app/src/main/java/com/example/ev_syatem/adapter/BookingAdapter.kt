package com.example.ev_syatem.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ev_syatem.databinding.ItemBookingBinding
import com.example.ev_syatem.data.Booking

class BookingAdapter(
    private val bookings: List<Booking>,
    private val onModifyClick: ((Booking) -> Unit)? = null,
    private val onCancelClick: ((Booking) -> Unit)? = null
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            // Assign values to views
            binding.tvStationName.text = "Station: ${booking.stationId}"
            binding.tvBookingDate.text = booking.reservationTime.split("T")[0]
            binding.tvBookingTime.text = booking.reservationTime.split("T")[1].substring(0, 5)
            binding.tvBookingStatus.text = booking.status

            // Change color by status
            when (booking.status.lowercase()) {
                "completed" -> binding.tvBookingStatus.setBackgroundColor(Color.parseColor("#10B981"))
                "cancelled" -> binding.tvBookingStatus.setBackgroundColor(Color.parseColor("#F87171"))
                "pending" -> binding.tvBookingStatus.setBackgroundColor(Color.parseColor("#F59E0B"))
                else -> binding.tvBookingStatus.setBackgroundColor(Color.parseColor("#475569"))
            }

            // Show or hide buttons based on listeners
            binding.btnModify.visibility = if (onModifyClick != null) View.VISIBLE else View.GONE
            binding.btnCancel.visibility = if (onCancelClick != null) View.VISIBLE else View.GONE

            // Button click listeners
            binding.btnModify.setOnClickListener { onModifyClick?.invoke(booking) }
            binding.btnCancel.setOnClickListener { onCancelClick?.invoke(booking) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size
}
