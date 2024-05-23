package com.aditd5.mov.view.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditd5.mov.databinding.ActivityDetailBinding
import com.aditd5.mov.model.Movie
import com.aditd5.mov.view.SelectSeatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private lateinit var db: FirebaseFirestore

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

        mainButton()
        getData()
        setPlayerData()
    }

    @Suppress("DEPRECATION")
    private fun getData() {
        movie = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("movie", Movie::class.java)
        } else {
            intent.getParcelableExtra("movie")
        }
        val status = intent.getStringExtra("status")

        binding.apply {
            tvMovieName.text = movie?.title
            tvMovieGenre.text = movie?.genre
            tvRating.text = movie?.rating
            tvStoryboard.text = movie?.synopsis

            Picasso.get()
                .load(movie?.posterUrl)
                .into(ivMoviePoster)

            if (status == "soon") {
                btnNext.visibility = View.INVISIBLE
                tvRating.visibility = View.INVISIBLE
            }

            val playerAdapter = PlayerAdapter(movie!!.actors)     //movie?.let { PlayerAdapter(it.actors) }
            rvPlayer.apply {
                layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = playerAdapter
            }

//            binding.btnNext.setOnClickListener {
//                val intent = Intent(this@DetailActivity, SelectSeatActivity::class.java)
//                intent.putExtra("movie", movie)
//                startActivity(intent)
//            }
        }
    }

    private fun setPlayerData() {
//        database.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                val playerList = mutableListOf<Player>()
//
//                for (getDataSnapshot in snapshot.children) {
//                    val player = getDataSnapshot.getValue(Player::class.java)
//                    player.let {
//                        playerList.add(it!!)
//                    }
//                }
//                setPlayerData(playerList)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                    Toast.makeText(
//                        this@DetailActivity,
//                        "Error ${error.message}"
//                        , Toast.LENGTH_SHORT
//                    ).show()
//            }
//        })

//        val docRef = db.collection("movies")
//            .document("dhwBP2adWbzcKdgBPJhE")
//            .collection("aktor")
//
//            docRef.get()
//                .addOnSuccessListener {
//                    if (!it.isEmpty) {
//                        val actorsList = mutableListOf<Aktor>()
//
//                        for (document in it) {
//                            val actors = document.toObject(Aktor::class.java)
//                            actorsList.add(actors)
//                        }
//
//                        setPlayerData(actorsList)
//                    } else {
//                        Toast.makeText(this, "Kosong om", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                .addOnFailureListener {
//                    Toast.makeText(this, "Lah error", Toast.LENGTH_SHORT).show()
//                }
    }

//    private fun setPlayerData(playerList: List<Actor>) {
//        val playerAdapter = PlayerAdapter(playerList)
//        binding.rvPlayer.apply {
//            layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
//            adapter = playerAdapter
//        }
//    }

    private fun mainButton() {
        binding.btnNext.setOnClickListener {
            val intent = Intent(this, SelectSeatActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }
    }
}