package com.aditd5.mov.view.ticket

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditd5.mov.R
import com.aditd5.mov.databinding.FragmentTicketBinding
import com.aditd5.mov.model.Movie
import com.aditd5.mov.model.Transaction
import com.aditd5.mov.util.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class TicketFragment : Fragment() {

    private var _binding: FragmentTicketBinding? = null

    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    private val activeTicketList = mutableListOf<Transaction>()
    private val historyTicketList = mutableListOf<Transaction>()

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

        getTransactions()
        setButtonListener()

        setButtonLine(binding.btnActiveTicket)
    }

    private fun getTransactions() {
        binding.shimmerView.startShimmer()

        val today = LocalDate.now()
        val todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant())

        db.collection("transactions").whereEqualTo("userId" , user!!.uid).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    for (document in snapshot) {
                        val transaction = document.toObject(Transaction::class.java).also {
                            it.transactionId = document.id
                        }

                        db.collection("movies").document(transaction.movieId!!).get()
                            .addOnSuccessListener { movieSnapshot ->
                                if (movieSnapshot.exists()) {
                                    val movie = movieSnapshot.toObject(Movie::class.java)
                                    transaction.movie = movie

                                    if (isAdded) {
                                        transaction.showTime?.let { date ->
                                            if (date.toDate().before(todayDate)) {
                                                historyTicketList.add(transaction)
                                            } else {
                                                activeTicketList.add(transaction)
                                                setActiveTicket(activeTicketList)
                                            }
                                        }
                                    }
                                    activeTicketList.sortBy { it.showTime }
                                    historyTicketList.sortByDescending { it.showTime }

                                    if (activeTicketList.isEmpty()) {
                                        setEmptyTicketInfo()
                                    }

                                    stopShimmer()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Data film tidak ditemukan",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.e("ticket", "Data film tidak ditemukan")

                                    stopShimmer()
                                }
                            }
                            .addOnFailureListener { e ->
                                if (isAdded) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Gagal mendapatkan data film",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                Log.e("ticket", "get movie failure", e)

                                stopShimmer()
                            }
                    }
                } else {
                    stopShimmer()
                    setEmptyTicketInfo()
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Gagal mendapatkan data transaksi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e("ticket", "get transaction failure", e)

                stopShimmer()
            }
    }

    private fun setButtonListener() {
        binding.apply {
            btnActiveTicket.setOnClickListener {
                setButtonLine(btnActiveTicket)
                setActiveTicket(activeTicketList)

                if (activeTicketList.isEmpty()) {
                    setEmptyTicketInfo()
                }
            }

            btnTicketHistory.setOnClickListener {
                setButtonLine(btnTicketHistory)
                setTicketHistory(historyTicketList)

                if (historyTicketList.isNotEmpty()) {
                    tvInformation.isVisible = false
                }
            }
        }
    }

    private fun setButtonLine(button: Button) {
        binding.apply {
            btnActiveTicketLine.visibility = View.INVISIBLE
            btnTicketHistoryLine.visibility = View.INVISIBLE

            // Set semua teks tombol menjadi normal
            btnActiveTicket.setTypeface(null, Typeface.NORMAL)
            btnTicketHistory.setTypeface(null, Typeface.NORMAL)

            // Tampilkan garis di bawah tombol yang diklik dan set tombol menjadi bold
            when (button) {
                btnActiveTicket -> {
                    btnActiveTicketLine.visibility = View.VISIBLE
                    btnActiveTicket.setTypeface(null, Typeface.BOLD)
                }
                btnTicketHistory -> {
                    btnTicketHistoryLine.visibility = View.VISIBLE
                    btnTicketHistory.setTypeface(null, Typeface.BOLD)
                }
            }
        }
    }

    private fun setActiveTicket(list: List<Transaction>) {
        val mAdapter = ActiveTicketAdapter(list)
        binding.rvTicket.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = mAdapter
        }
        binding.tvInformation.isVisible = false
    }

    private fun setTicketHistory(list: List<Transaction>) {
        val mAdapter = HistoryTicketAdapter(list)
        binding.rvTicket.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = mAdapter
        }
    }

    private fun setEmptyTicketInfo() {
        if (isAdded) {
            binding.apply {
                tvInformation.isVisible = true
                if (Prefs.isLogin) {
                    tvInformation.text = getString(R.string.empty_ticket)
                } else {
                    tvInformation.text = getString(R.string.empty_ticket_guest)
                }
            }
        }
    }

    private fun stopShimmer() {
        if (isAdded) {
            binding.apply {
                shimmerView.isVisible = false
                shimmerView.stopShimmer()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.shimmerView.stopShimmer()
        _binding = null
    }
}