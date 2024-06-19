package com.aditd5.mov.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.gridlayout.widget.GridLayout
import com.aditd5.mov.R
import com.aditd5.mov.databinding.ActivitySelectSeatBinding
import com.aditd5.mov.model.Movie
import com.aditd5.mov.view.checkout.CheckoutActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Suppress("DEPRECATION")
@SuppressLint("SetTextI18n")
class SelectSeatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectSeatBinding

    private lateinit var db: FirebaseFirestore
    private lateinit var listener: ListenerRegistration

//    private val bookedSeats = setOf("A1", "A3", "C2")
    private var bookedSeats = setOf("")
    private var selectedSeats = mutableSetOf<String>()

    private var movie : Movie? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectSeatBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        setTitle()
//        getRealtimeSeat()
        getSeat()
//        setSeat()
    }

    private fun setTitle() {
        movie = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("movie", Movie::class.java)
        } else {
            intent.getParcelableExtra("movie")
        }
        binding.tvTitle.text = movie?.title
    }

    private fun getRealtimeSeat() {
        val docRef = db.collection("transactions")
        listener = docRef.addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(
                    this,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
                return@addSnapshotListener
            }

            if (value != null) {
                var kursi = mutableListOf<String>()
                for (data in value) {
                    kursi = data.get("seats") as MutableList<String>
        //                    Toast.makeText(
        //                        this,
        //                        kursi.toString(),
        //                        Toast.LENGTH_SHORT
        //                    ).show()
//                    bookedSeats = kursi.toSet()
                }
//                setSeat(kursi.toSet())
            }
        }
    }

    private fun getSeat() {
        val docRef = db.collection("transactions")
//        docRef.get()
//            .addOnSuccessListener { querySnapshot ->
//                val tasks: MutableList<Task<List<String>>> = mutableListOf()
//
//                for (document in querySnapshot.documents) {
//                    val arrayTask: Task<List<String>> = document.get("seats")?.let { fieldValue ->
//                        if (fieldValue is List<*>) {
//                            Tasks.forResult(fieldValue.filterIsInstance<String>())
//                        } else {
//                            Tasks.forResult(emptyList())
//                        }
//                    } ?: Tasks.forResult(emptyList())
//                    tasks.add(arrayTask)
//                }
//
//                Tasks.whenAllSuccess<List<String>>(tasks)
//                    .addOnSuccessListener {
//                        val values = it.flatten()
//                        Toast.makeText(this, values.toString(), Toast.LENGTH_SHORT).show()
//                        setSeat(values.toMutableSet())
//                    }
//                    .addOnFailureListener {
//                        Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
//                    }
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
//            }
        listener = docRef.addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(
                    this,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
                return@addSnapshotListener
            }

            if (value != null) {
                val tasks: MutableList<List<String>> = mutableListOf()

                for (document in value.documents) {
                    val fieldValue = document.get("seats")
                    if (fieldValue is List<*>) {
                        tasks.add(fieldValue.filterIsInstance<String>())
                    }
                }

                val allValues = tasks.flatten()
                Toast.makeText(this, allValues.toString(), Toast.LENGTH_SHORT).show()
                setSeat(allValues.toMutableSet())
            }
        }
    }

    private fun setSeat(bookedSeats: Set<String>) {
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
                            Toast.makeText(
                                this,
                                "Maksimal 5 kursi dalam 1 pesanan",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            selectedSeats.add(seatNumber)
                            seat.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPink))
                            val selectedSeat = "Seat $seatNumber selected"
                            Toast.makeText(
                                this,
                                selectedSeat,
                                Toast.LENGTH_SHORT
                            ).show()
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


    private fun setButton() {
        binding.apply {
            if (selectedSeats.isNotEmpty()) {
                btnCheckout.visibility = View.VISIBLE
                btnCheckout.text = "Pilih Kursi (${selectedSeats.size})"
                btnCheckout.setOnClickListener {
                    val seats = ArrayList(selectedSeats)
                    val intent = Intent(this@SelectSeatActivity, CheckoutActivity::class.java)
                    intent.putExtra("movie", movie)
                    intent.putExtra("seats", seats)
                    startActivity(intent)
                }
            } else {
                btnCheckout.visibility = View.INVISIBLE
            }
        }
    }
}