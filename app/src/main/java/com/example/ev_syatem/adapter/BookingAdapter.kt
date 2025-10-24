package com.example.ev_syatem.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ev_syatem.data.Booking
import com.example.ev_syatem.databinding.ItemBookingBinding

class BookingAdapter(
    private val bookings: List<Booking>,
    private val onItemClick: ((Booking) -> Unit),
    private val onModifyClick: ((Booking) -> Unit)? = null,
    private val onCancelClick: ((Booking) -> Unit)? = null
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            // Your existing code to set text and colors is correct
            binding.tvStationName.text = "Station: ${booking.stationId}"
            binding.tvBookingDate.text = booking.reservationTime.split("T")[0]
            binding.tvBookingTime.text = booking.reservationTime.split("T")[1].substring(0, 5)
            binding.tvBookingStatus.text = booking.status

            when (booking.status.lowercase()) {
                "completed" -> binding.tvBookingStatus.setBackgroundColor(Color.parseColor("#10B981"))
                "cancelled" -> binding.tvBookingStatus.setBackgroundColor(Color.parseColor("#F87171"))
                "pending" -> binding.tvBookingStatus.setBackgroundColor(Color.parseColor("#F59E0B"))
                "approved" -> binding.tvBookingStatus.setBackgroundColor(Color.parseColor("#3B82F6"))
                else -> binding.tvBookingStatus.setBackgroundColor(Color.parseColor("#475569"))
            }

            // Your button visibility logic is also correct
            if (booking.status.equals("Pending", ignoreCase = true)) {
                binding.btnModify.visibility = if (onModifyClick != null) View.VISIBLE else View.GONE
                binding.btnCancel.visibility = if (onCancelClick != null) View.VISIBLE else View.GONE
            } else {
                binding.btnModify.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
            }

            binding.btnModify.setOnClickListener { onModifyClick?.invoke(booking) }
            binding.btnCancel.setOnClickListener { onCancelClick?.invoke(booking) }

            // ✅ FIX: Only trigger the click listener if the status is "Approved"
            itemView.setOnClickListener {
                if (booking.status.equals("Approved", ignoreCase = true)) {
                    onItemClick(booking)
                }
                // If the status is not "Approved", nothing happens when the item is clicked.
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding =
            ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size
}
