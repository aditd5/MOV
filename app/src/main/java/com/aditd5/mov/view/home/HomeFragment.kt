package com.aditd5.mov.view.home

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditd5.mov.databinding.FragmentHomeBinding
import com.aditd5.mov.model.Movie
import com.aditd5.mov.util.CurrencyFormatter
import com.aditd5.mov.util.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.squareup.picasso.Picasso

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore

    private lateinit var listener: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        db = FirebaseFirestore.getInstance()

        setUserData()
        getRealtimeMovies()
    }

    private fun setUserData() {
        val login = Prefs.isLogin
        val name = Prefs.name
        val uri = user?.photoUrl

        if (user != null) {
            binding.tvName.text = name

            if (uri != null) {
                Picasso.get()
                    .load(uri)
                    .into(binding.ivImgProfile)
            }
            getBalance()
        }
    }

    private fun getBalance() {
        val docRef = db.collection("users").document(user!!.uid)
        listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val isActive = snapshot.get("wallet")
                if (isActive == true) {
                    val mBalance = snapshot.get("balance")
                    val balance = mBalance.toString().toInt()
                    if (isAdded) {
                        binding.tvBalance.text = CurrencyFormatter().numberFormat(balance)
                    }
                }
            } else {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Data wallet tidak ditemukan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getRealtimeMovies() {
        val docRef = db.collection("movies")
        listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val nowPlayingList = mutableListOf<Movie>()
                val comingSoonList = mutableListOf<Movie>()
                val today = Calendar.getInstance().timeInMillis

                for (document in snapshot) {
                    val movie = document.toObject(Movie::class.java).also {
                        it.movieId = document.id
                    }
                    val releaseDate = movie.releaseDate?.toDate()?.time ?:0

                    if (releaseDate <= today) {
                        nowPlayingList.add(movie)
                    } else {
                        comingSoonList.add(movie)
                    }
                }

                if (isAdded) {
                    setNowPlayingMovie(nowPlayingList)
                    setComingSoonMovie(comingSoonList)
                }
            } else {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Data wallet tidak ditemukan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setNowPlayingMovie(filmList: List<Movie>) {
        val movieAdapter = MovieAdapter(filmList, "now")
        binding.rvNowplaying.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
            adapter = movieAdapter
        }
    }

    private fun setComingSoonMovie(filmList: List<Movie>) {
        val movieAdapter = MovieAdapter(filmList, "soon")
        binding.rvComingSoon.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
            adapter = movieAdapter
        }
    }

    override fun onStop() {
        super.onStop()
        listener.remove()  //mematikan realtime update ketika fragment tidak dibuka
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}