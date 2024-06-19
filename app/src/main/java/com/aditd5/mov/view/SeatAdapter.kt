package com.aditd5.mov.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.aditd5.mov.R

class SeatAdapter(
    context: Context ,
    seats: List<String> ,
    private var occupiedSeats: List<String> ,
    private var selectedSeats: MutableList<String>
) : ArrayAdapter<String>(context, R.layout.item_seat, seats) {

    override fun getView(position: Int , convertView: View? , parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_seat , parent , false)
        val seatTextView = view.findViewById<TextView>(R.id.textViewSeat)
        val seat = getItem(position)

        seatTextView.text = seat

        if (occupiedSeats.contains(seat)) {
            view.isEnabled = false
            seatTextView.setBackgroundResource(R.color.colorSeatOccupied)
        } else if (selectedSeats.contains(seat)) {
            seatTextView.setBackgroundResource(R.color.colorSeatSelected)
        } else {
            seatTextView.setBackgroundResource(R.color.colorSeatAvailable)
        }

        return view
    }


    override fun isEnabled(position: Int): Boolean {
        val seat = getItem(position)
        return !occupiedSeats.contains(seat)
    }

    fun toggleSeat(seat: String) {
        if (selectedSeats.contains(seat)) {
            selectedSeats.remove(seat)
        } else {
            selectedSeats.add(seat)
        }
        notifyDataSetChanged()
    }
}