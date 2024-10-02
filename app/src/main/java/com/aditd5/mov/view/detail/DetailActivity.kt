package com.aditd5.mov.view.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditd5.mov.databinding.ActivityDetailBinding
import com.aditd5.mov.model.Movie
import com.aditd5.mov.util.Prefs
import com.aditd5.mov.view.seat.SelectSeatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
@SuppressLint("SetTextI18n")
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private lateinit var db: FirebaseFirestore

    private var selectedDate: String? = null
    private var selectedTime: String? = null

    private var movie: Movie? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        setButtonListener()
        getDataFromIntent()
        setDateTime()
    }

    private fun getDataFromIntent() {
        movie = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("movie", Movie::class.java)
        } else {
            intent.getParcelableExtra("movie")
        }
        val status = intent.getStringExtra("status")

        binding.apply {
            tvTitle.text = movie?.title
            tvMovieGenre.text = movie?.genres
            tvRating.text = "|  " + movie?.rating + "  |"
            tvRuntime.text = movie?.runtime.toString() + " Min"
            tvSynopsis.text = movie?.synopsis

            Picasso.get()
                .load(movie?.posterUrl)
                .into(ivMoviePoster)

            if (status == "upcoming") {
                rvDate.isVisible = false
                rvTime.isVisible = false
                btnNext.isVisible = false
            }

            val playerAdapter = PlayerAdapter(movie!!.cast)     //movie?.let { PlayerAdapter(it.actors) }
            rvPlayer.apply {
                layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = playerAdapter
            }
        }
    }

    private fun setDateTime() {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        val tomorrow = calendar.apply { add(Calendar.DAY_OF_YEAR, 1) }.time
        val dayAfterTomorrow = calendar.apply { add(Calendar.DAY_OF_YEAR, 1) }.time

        val dayFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
        val dateFormat = SimpleDateFormat("d", Locale("id", "ID"))
        val formatDate = SimpleDateFormat("dd MM yyyy", Locale("id", "ID"))

        val lastScreening = movie!!.lastScreeningDate!!.toDate()
        val lastScreeningFormatted = formatDate.format(lastScreening)
        val todayFormatted = formatDate.format(today)
        val tomorrowFormatted = formatDate.format(tomorrow)
        val dayAfterTomorrowFormatted = formatDate.format(dayAfterTomorrow)

        val dates =
            if (todayFormatted == lastScreeningFormatted) {
                listOf(
                    Triple(dayFormat.format(today), dateFormat.format(today), todayFormatted.toString())
                )
            } else {
                listOf(
                    Triple(dayFormat.format(today), dateFormat.format(today), todayFormatted.toString()),
                    Triple(dayFormat.format(tomorrow), dateFormat.format(tomorrow), tomorrowFormatted.toString()),
                    Triple(dayFormat.format(dayAfterTomorrow), dateFormat.format(dayAfterTomorrow), dayAfterTomorrowFormatted.toString())
                )
            }

        val dateAdapter = DateAdapter(dates) {
            selectedDate = it.third

            setButtonState()
        }

        binding.rvDate.adapter = dateAdapter
        binding.rvDate.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val times = listOf(
            "10:30",
            "13:30",
            "16:30",
            "19:30"
        )

        val timeAdapter = TimeAdapter(times) {
            selectedTime = it

            setButtonState()
        }

        binding.rvTime.adapter = timeAdapter
        binding.rvTime.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setButtonListener() {
        binding.apply {
            btnNext.setOnClickListener {
                val formattedDateTime = SimpleDateFormat("dd MM yyyy hh:mm", Locale("id", "ID")).parse("$selectedDate $selectedTime")
                val selectedDateTimeEpoch = formattedDateTime!!.time

                val intent = Intent(this@DetailActivity, SelectSeatActivity::class.java)
                intent.putExtra("movie", movie)
                intent.putExtra("dateTime", selectedDateTimeEpoch)
                startActivity(intent)
            }
        }
    }

    private fun setButtonState() {
        if (Prefs.isLogin && selectedDate != null && selectedTime != null) {
            binding.btnNext.isEnabled = true
        }
    }
}