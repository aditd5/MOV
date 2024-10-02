package com.aditd5.mov.view.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditd5.mov.R
import com.aditd5.mov.databinding.FragmentHomeBinding
import com.aditd5.mov.model.Movie
import com.aditd5.mov.util.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.util.Date

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore

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

        setRefresh()
        setUserData()
        getMovies()
    }

    private fun setRefresh() {
        if (isAdded) {
            binding.apply {
                swipeRefresh.setOnRefreshListener {
                    getMovies()
                    swipeRefresh.setColorSchemeColors(requireContext().getColor(R.color.background))
                }
            }
        }
    }

    private fun setUserData() {
        val login = Prefs.isLogin
        val fullname = "${Prefs.firstName} ${Prefs.lastName}"
        val uri = Prefs.imgProfileUri

        if (login) {
            binding.tvName.text = getString(R.string.hello , fullname)

            if (uri != null) {
                Picasso.get()
                    .load(uri)
                    .into(binding.ivImgProfile)
            }
        }
    }

    private fun getMovies() {
        binding.shimmerViewUpcoming.startShimmer()
        binding.shimmerViewNowplaying.startShimmer()

        db.collection("movies").get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val nowPlayingList = mutableListOf<Movie>()
                    val upComingList = mutableListOf<Movie>()

                    val today = Date()

                    for (document in snapshot) {
                        val movie = document.toObject(Movie::class.java).also {
                            it.movieId = document.id
                        }
                        val releaseDate = movie.releaseDate!!.toDate()
                        val lastScreeningDate = movie.lastScreeningDate!!.toDate()

                        if (today in releaseDate..< lastScreeningDate) {
                            nowPlayingList.add(movie)
                        } else if (releaseDate > today) {
                            upComingList.add(movie)
                        }
                    }

                    if (isAdded) {
                        setNowPlayingMovie(nowPlayingList)
                        setUpComingMovie(upComingList)
                        stopShimmer()
                        binding.swipeRefresh.isRefreshing = false
                    }
                } else {
                    if (isAdded) {
                        Toast.makeText(
                            requireContext() ,
                            getString(R.string.system_maintenance_warning) ,
                            Toast.LENGTH_SHORT
                        ).show()
                        stopShimmer()
                        binding.swipeRefresh.isRefreshing = false
                        Log.e("get movies error", "Empty Movies database")
                    }
                }
            }
            .addOnFailureListener { error ->
                if (isAdded) {
                    Toast.makeText(
                        requireContext() ,
                        error.message,
                        Toast.LENGTH_SHORT
                        ).show()
                    stopShimmer()
                    binding.swipeRefresh.isRefreshing = false
                }
                Log.e("get movies failure", error.message ?: "Error")
            }
    }

    private fun setNowPlayingMovie(filmList: List<Movie>) {
        val movieAdapter = MovieAdapter(filmList, "now")
        binding.rvNowplaying.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
            adapter = movieAdapter
        }
    }

    private fun setUpComingMovie(filmList: List<Movie>) {
        val movieAdapter = MovieAdapter(filmList, "upcoming")
        binding.rvUpComing.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
            adapter = movieAdapter
        }
    }

    private fun stopShimmer() {
        binding.apply {
            shimmerViewUpcoming.isVisible = false
            shimmerViewNowplaying.isVisible = false
            shimmerViewUpcoming.stopShimmer()
            shimmerViewNowplaying.stopShimmer()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}