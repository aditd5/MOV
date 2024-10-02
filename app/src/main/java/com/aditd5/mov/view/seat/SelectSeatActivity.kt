package com.aditd5.mov.view.seat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.aditd5.mov.R
import com.aditd5.mov.databinding.ActivitySelectSeatBinding
import com.aditd5.mov.model.Movie
import com.aditd5.mov.view.checkout.CheckoutActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Date

@Suppress("DEPRECATION")
class SelectSeatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectSeatBinding

    private lateinit var db: FirebaseFirestore

    private lateinit var listener: ListenerRegistration

    private var selectedDateEpoch: Long? = 0L

    private var selectedSeats = mutableListOf<String>()
    private var seats = mutableListOf<String>()
    private val maxSelectedSeats = 5

    private var movie : Movie? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectSeatBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root

        )
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left , systemBars.top , systemBars.right , systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        binding.shimmerView.startShimmer()

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
        selectedDateEpoch = intent.getLongExtra("dateTime", 0L)

        binding.tvTitle.text = movie?.title
    }

    private fun generateSeatLabels() {
        for (row in 'A'..'J') {
            for (column in 1..10) {
                seats.add("$row$column")
            }
        }
    }

    private fun getOccupiedSeats() {
        val timestampFromEpoch = Timestamp(Date(selectedDateEpoch!!))
        listener = db.collection("transactions").whereEqualTo("showTime" , timestampFromEpoch)
            .addSnapshotListener { value , error ->
                if (error != null) {
                    Toast.makeText(
                        this ,
                        error.message ,
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("select seat", "get occupied seats error", error)
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
                }
            }
    }

    private fun setupGridView(occupiedSeats: List<String>) {
        val seatAdapter = SeatAdapter(this , seats , occupiedSeats , selectedSeats)

        binding.apply {
            gridView.adapter = seatAdapter

            shimmerView.stopShimmer()
            shimmerView.isVisible = false

            tvSeatAvailable.isVisible = true
            tvSeatOccupied.isVisible = true
            tvSeatSelected.isVisible = true
        }

        binding.gridView.onItemClickListener = AdapterView.OnItemClickListener { _ , _ , position , _ ->
            val seat = seats[position]
            if (!occupiedSeats.contains(seat)) {
                if (selectedSeats.contains(seat) || selectedSeats.size < maxSelectedSeats) {
                    seatAdapter.toggleSeat(seat)
                    setCheckoutButton()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.max_seat_warning),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setCheckoutButton() {
        binding.apply {
            if (selectedSeats.isNotEmpty()) {
                btnCheckout.isEnabled = true
                btnCheckout.text = getString(R.string.select_seat_btn , selectedSeats.size.toString())
                btnCheckout.setOnClickListener {
                    val seats = ArrayList(selectedSeats)
                    val intent = Intent(this@SelectSeatActivity, CheckoutActivity::class.java)
                    intent.putExtra("movie", movie)
                    intent.putExtra("seats", seats)
                    intent.putExtra("dateTime", selectedDateEpoch)
                    startActivity(intent)
                }
            } else {
                btnCheckout.isEnabled = false
                btnCheckout.text = getString(R.string.select_seat)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        listener.remove()
    }
}