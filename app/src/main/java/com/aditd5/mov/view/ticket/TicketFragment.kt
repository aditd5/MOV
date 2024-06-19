package com.aditd5.mov.view.ticket

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.aditd5.mov.databinding.FragmentTicketBinding
import com.aditd5.mov.model.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TicketFragment : Fragment() {

    private var _binding: FragmentTicketBinding? = null

    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    private lateinit var listener: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTicketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        getRealtimeTransactions()
    }

    private fun getRealtimeTransactions() {
        val docRef = db.collection("transactions").whereEqualTo("userId" , user?.uid)
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
                val ticketList = mutableListOf<Movie>()
                val comingSoonList = mutableListOf<Movie>()
                val today = Calendar.getInstance().timeInMillis

                for (document in snapshot) {
//                    val movie = document.toObject(Movie::class.java).also {
//                        it.movieId = document.id
//                    }
//                    val releaseDate = movie.releaseDate?.toDate()?.time ?:0
//
//                    if (releaseDate <= today) {
//                        nowPlayingList.add(movie)
//                    } else {
//                        comingSoonList.add(movie)
//                    }
                }

//                if (isAdded) {
//                    setNowPlayingMovie(nowPlayingList)
//                    setComingSoonMovie(comingSoonList)
//                }
            } else {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Data transaksi tidak ditemukan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

//    private fun setNowPlayingMovie(filmList: List<Movie>) {
//        val movieAdapter = MovieAdapter(filmList, "now")
//        binding.rvNowplaying.apply {
//            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
//            adapter = movieAdapter
//        }
//    }

    override fun onStop() {
        super.onStop()
        listener.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}