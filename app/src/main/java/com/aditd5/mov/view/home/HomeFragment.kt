package com.aditd5.mov.view.home

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditd5.mov.databinding.FragmentHomeBinding
import com.aditd5.mov.model.Movie
import com.aditd5.mov.util.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore

    private var listener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        view?.fitsSystemWindows = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        WindowCompat.getInsetsController(requireActivity().window, view).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        setUserData()
//        getMovies()
        getRealtimeMovies()
    }

    private fun setUserData() {
        val login = Prefs.isLogin
        val name = Prefs.name
        val uri = auth.currentUser!!.photoUrl

        if (login) {
            binding.tvName.text = name

            if (uri != null) {
                Picasso.get()
                    .load(uri)
                    .into(binding.ivImgProfile)
            }
        }
    }

    private fun getMovies() {
        val docRef = db.collection("movies")
        docRef.get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val nowPlayingList = mutableListOf<Movie>()
                    val comingSoonList = mutableListOf<Movie>()
                    val today = Calendar.getInstance().timeInMillis

                    for (document in result) {
                        val movie = document.toObject(Movie::class.java)
                        val releaseDate = movie.releaseDate?.toDate()?.time ?:0

                        if (releaseDate > today) {
                            nowPlayingList.add(movie)
                        } else {
                            comingSoonList.add(movie)
                        }

//                        db.collection("movies").document(document.id).collection("aktor")
//                            .get()
//                            .addOnSuccessListener {
//                                val actorsList = mutableListOf<Actor>()
//                                for (doc in it) {
//                                    val actor = doc.toObject(Actor::class.java)
//                                    actorsList.add(actor)
//                                }
//                            }
                    }

                    if (isAdded) {
                        setNowPlayingMovie(nowPlayingList)
                        setComingSoonMovie(comingSoonList)
                    }
                } else {
                    if (isAdded) {
                        Toast.makeText(
                            requireContext(),
                            "Database Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                val errorMessage = exception.toString()
                Log.e("get movies", errorMessage)
               if (isAdded) {
                   Toast.makeText(
                       requireActivity(),
                       "Error $errorMessage",
                       Toast.LENGTH_SHORT
                   ).show()
               }
            }
    }

    private fun getRealtimeMovies() {
        val docRef = db.collection("movies")
        listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(
                    requireContext(),
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val nowPlayingList = mutableListOf<Movie>()
                val comingSoonList = mutableListOf<Movie>()
                val today = Calendar.getInstance().timeInMillis

                for (document in snapshot) {
                    val movie = document.toObject(Movie::class.java)
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
                        "Error, data film kosong",
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
        listener?.remove()  //mematikan realtime update ketika fragment tidak dibuka
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}