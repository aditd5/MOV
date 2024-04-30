package com.aditd5.mov.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import com.aditd5.mov.R
import com.aditd5.mov.databinding.ActivitySelectSeatBinding
import com.aditd5.mov.view.checkout.CheckoutActivity

class SelectSeatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectSeatBinding

    private val bookedSeats = setOf("A1", "A3", "C2")
    private val selectedSeats = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectSeatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTitle()
        setSeat()
    }

    private fun setTitle() {
        val title = intent.getStringExtra("title")
        binding.tvTitle.text = title
    }

    private fun setSeat() {
        val gridLayout: GridLayout = binding.seatLayout
        val column = 10
        val row = 7

        for (i in 0 until row) {
            for (j in 0 until column) {
                val seatNumber = "${('A' + i)}${j + 1}"
                val seat = createSeatButton(seatNumber)

                if (bookedSeats.contains(seatNumber)) {
                    seat.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlueGrey))
                    seat.isClickable = false
                } else {
                    seat.setBackgroundColor(ContextCompat.getColor(this, R.color.whiteGrey))
                    seat.setOnClickListener {
                        if (selectedSeats.contains(seatNumber)) {
                            selectedSeats.remove(seatNumber)
                            seat.setBackgroundColor(ContextCompat.getColor(this, R.color.whiteGrey))
                            setButton()
                        } else if (selectedSeats.size > 4) {
                            seat.isClickable = false
                            Toast.makeText(this, "Maksimal 5 kursi dalam 1 pesanan", Toast.LENGTH_SHORT).show()
                        } else {
                            selectedSeats.add(seatNumber)
                            seat.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPink))
                            val selectedSeat = "Seat $seatNumber selected"
                            Toast.makeText(this, selectedSeat, Toast.LENGTH_SHORT).show()
                            setButton()
                        }
                    }
                }
                gridLayout.addView(seat)
            }
        }
    }

    private fun createSeatButton(seatNumber: String): Button {
        return Button(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 50
                height = 50
                bottomMargin = 10
                topMargin = 10
                leftMargin = 10
                rightMargin = 10
            }
            text = seatNumber
            setTextColor(Color.WHITE)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setButton() {
        binding.apply {

            if (selectedSeats.isNotEmpty()) {
                btnCheckout.visibility = View.VISIBLE
                btnCheckout.text = "Pilih Kursi (${selectedSeats.size})"
                btnCheckout.setOnClickListener {
                    val seats = ArrayList(selectedSeats)
                    val intent = Intent(this@SelectSeatActivity, CheckoutActivity::class.java)
                    intent.putExtra("seats", seats)
                    startActivity(intent)
                }
            } else {
                btnCheckout.visibility = View.INVISIBLE
            }
        }
    }
}