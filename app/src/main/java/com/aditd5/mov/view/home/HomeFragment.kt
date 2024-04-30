package com.aditd5.mov.view.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditd5.mov.databinding.FragmentHomeBinding
import com.aditd5.mov.model.Film
import com.aditd5.mov.util.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

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

        database = FirebaseDatabase.getInstance().getReference("Film")
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

//        binding.ivImgProfile.setOnClickListener {
//            val transaction = parentFragmentManager.beginTransaction()
//            transaction.replace(R.id.nav_host_fragment_activity_home, ProfileFragment())
//            transaction.addToBackStack(null)
//            transaction.commit()
//        }

        setUsername()
        getMovieData()
    }

    private fun setUsername() {
        val user = auth.currentUser
        val uid = user?.uid
        val name = Prefs.name
        val uri = Prefs.imgProfileUri

        if (user != null) {
            binding.tvName.text = name
        }

        if (uri != null) {
            Picasso.get()
                .load(uri)
                .into(binding.ivImgProfile)
        } else {
            displayImgProfileFromFirebase(uid!!)
        }
    }

    private fun displayImgProfileFromFirebase(uid: String) {
        val imageRef = storage.reference.child("img_user/$uid.jpg")

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            if (isAdded) {
                binding.loading.visibility = View.INVISIBLE
                Prefs.imgProfileUri = uri.toString()
                Picasso.get()
                    .load(uri)
                    .into(binding.ivImgProfile)
            }
        } .addOnFailureListener {
            Log.e("imgProfile", it.message.toString())
            Prefs.imgProfileUri = null
        }
    }

    private fun getMovieData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val filmList = mutableListOf<Film>()

                for (getDataSnapshot in snapshot.children) {
                    val film = getDataSnapshot.getValue(Film::class.java)
                    film?.let {
                        filmList.add(it)
                    }
                }

                if (isAdded) {
                    setMovieData(filmList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(
                        activity,
                        "Error ${error.message}"
                        , Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun setMovieData(filmList: List<Film>) {
           val movieAdapter = NowPlayingAdapter(filmList)
           binding.rvNowplaying.apply {
               layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
               adapter = movieAdapter
           }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}