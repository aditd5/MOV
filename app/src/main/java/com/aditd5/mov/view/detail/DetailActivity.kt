package com.aditd5.mov.view.detail

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditd5.mov.databinding.ActivityDetailBinding
import com.aditd5.mov.model.Player
import com.aditd5.mov.view.SelectSeatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private lateinit var movieTitle: String

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainButton()
        getData()
        setPlayerData()
    }

    private fun getData() {
        val title = intent.getStringExtra("title")
        val genre = intent.getStringExtra("genre")
        val rating = intent.getStringExtra("rating")
        val posterUrl = intent.getStringExtra("posterUrl")
        val desc = intent.getStringExtra("desc")

        database = FirebaseDatabase.getInstance().getReference("Film")
            .child(title.toString())
            .child("play")

        movieTitle = title.toString()

        binding.apply {
            tvMovieName.text = title
            tvMovieGenre.text = genre
            tvRating.text = rating
            tvStoryboard.text = desc

            Picasso.get()
                .load(posterUrl)
                .into(ivMoviePoster)
        }
    }

    private fun setPlayerData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val playerList = mutableListOf<Player>()

                for (getDataSnapshot in snapshot.children) {
                    val player = getDataSnapshot.getValue(Player::class.java)
                    player.let {
                        playerList.add(it!!)
                    }
                }
                setPlayerData(playerList)
            }

            override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@DetailActivity,
                        "Error ${error.message}"
                        , Toast.LENGTH_SHORT
                    ).show()
            }
        })
    }

    private fun setPlayerData(playerList: List<Player>) {
        val playerAdapter = PlayerAdapter(playerList)
        binding.rvPlayer.apply {
            layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerAdapter
        }
    }

    private fun mainButton() {
        binding.btnNext.setOnClickListener {
            val intent = Intent(this, SelectSeatActivity::class.java)
            intent.putExtra("title", movieTitle)
            startActivity(intent)
        }
    }
}