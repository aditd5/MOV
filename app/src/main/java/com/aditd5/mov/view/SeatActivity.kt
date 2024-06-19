package com.aditd5.mov.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aditd5.mov.R
import com.aditd5.mov.databinding.ActivitySeatBinding
import com.aditd5.mov.model.Movie
import com.aditd5.mov.view.checkout.CheckoutActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Suppress("DEPRECATION")
class SeatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeatBinding

    private lateinit var listener: ListenerRegistration

    private var selectedSeats = mutableListOf<String>()
    private var seats = mutableListOf<String>()
    private val maxSelectedSeats = 5

    private var movie : Movie? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeatBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root

        )
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left , systemBars.top , systemBars.right , systemBars.bottom)
            insets
        }

        getIntentData()
        generateSeatLabels()
        getOccupiedSeats()
    }

    private fun getIntentData() {
        movie = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("movie", Movie::class.java)
        } else {
            intent.getParcelableExtra("movie")
        }
        binding.tvTitle.text = movie?.title
    }

    private fun generateSeatLabels() {
        for (row in 'A'..'J') {
            for (col in 1..10) {
                seats.add("$row$col")
            }
        }
    }

    private fun getOccupiedSeats() {
        val docRef = FirebaseFirestore.getInstance().collection("transactions")
        listener = docRef.addSnapshotListener { value , error ->
            if (error != null) {
                Toast.makeText(
                    this ,
                    error.message ,
                    Toast.LENGTH_SHORT
                ).show()
                return@addSnapshotListener
            }

            if (value != null) {
                val occupiedSeats: MutableList<List<String>> = mutableListOf()

                for (document in value.documents) {
                    val fieldValue = document.get("seats")
                    if (fieldValue is List<*>) {
                        occupiedSeats.add(fieldValue.filterIsInstance<String>())
                    }
                }

                val occupiedSeatsList = occupiedSeats.flatten()
                setupGridView(occupiedSeatsList)

                selectedSeats.clear()
                binding.btnCheckout.visibility = View.INVISIBLE
            }
        }
    }

    private fun setupGridView(occupiedSeats: List<String>) {
        val seatAdapter = SeatAdapter(this , seats , occupiedSeats , selectedSeats)
        binding.gridView.adapter = seatAdapter

        binding.gridView.onItemClickListener = AdapterView.OnItemClickListener { _ , _ , position , _ ->
            val seat = seats[position]
            if (!occupiedSeats.contains(seat)) {
                if (selectedSeats.contains(seat) || selectedSeats.size < maxSelectedSeats) {
                    seatAdapter.toggleSeat(seat)
                    setCheckoutButton()
                } else {
                    Toast.makeText(
                        this,
                        "Hanya dapat memilih 5 kursi dalam 1 pesanan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setCheckoutButton() {
        binding.apply {
            if (selectedSeats.isNotEmpty()) {
                btnCheckout.visibility = View.VISIBLE
                btnCheckout.text = "Pilih Kursi (${selectedSeats.size})"
                btnCheckout.setOnClickListener {
                    val seats = ArrayList(selectedSeats)
                    val intent = Intent(this@SeatActivity, CheckoutActivity::class.java)
                    intent.putExtra("movie", movie)
                    intent.putExtra("seats", seats)
                    startActivity(intent)
                }
            } else {
                btnCheckout.visibility = View.INVISIBLE
            }
        }
    }

    override fun onStop() {
        super.onStop()
        listener.remove()
    }
}