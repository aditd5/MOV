package com.aditd5.mov.view.checkout

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditd5.mov.R
import com.aditd5.mov.R.style
import com.aditd5.mov.databinding.ActivityCheckoutBinding
import com.aditd5.mov.databinding.CardTicketSuccessBinding
import com.aditd5.mov.model.Checkout
import com.aditd5.mov.model.Movie
import com.aditd5.mov.util.CurrencyFormatter
import com.aditd5.mov.util.LoadingDialog
import com.aditd5.mov.view.SeatActivity
import com.aditd5.mov.view.home.HomeActivity
import com.aditd5.mov.view.ticket.TicketFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Suppress("DEPRECATION")
class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var db: FirebaseFirestore
    private lateinit var listener: ListenerRegistration

    private var movie: Movie? = null
    private var balance: Int? = null
    private var totalPrice: Int? = null

    private lateinit var seats: ArrayList<String>

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        db = FirebaseFirestore.getInstance()

        loadingDialog = LoadingDialog(this)

        setButton()
        setData()
    }

    private fun setData() {
        movie = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("movie", Movie::class.java)
        } else {
            intent.getParcelableExtra("movie")
        }
        seats = intent.getStringArrayListExtra("seats")!!
        val price = movie!!.price

        val checkoutList: List<Checkout> = seats.map { Checkout(it, price) }

        val checkoutAdapter = CheckoutAdapter(checkoutList)

        binding.rvCheckout.apply {
            layoutManager = LinearLayoutManager(this@CheckoutActivity)
            adapter = checkoutAdapter
        }

        totalPrice = seats.size * price!!
        binding.tvTotalprice.text = CurrencyFormatter().numberFormat(totalPrice!!)

        getBalance()
    }

    private fun getBalance() {
        val docRef = db.collection("users").document(user.uid)
        listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(
                    this,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val isActive = snapshot.get("wallet")
                if (isActive == true) {
                    val mBalance = snapshot.get("balance")
                    val iBalance = mBalance.toString().toInt()
                    balance = iBalance
                    binding.tvBalance.text = CurrencyFormatter().numberFormat(iBalance)
                }
            } else {
                Toast.makeText(
                    this,
                    "Data wallet tidak ditemukan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setButton() {
        binding.apply {
            btnPay.setOnClickListener {
                loadingDialog.showLoading()
                if (balance!! < totalPrice!!) {
                    Toast.makeText(
                        this@CheckoutActivity,
                        "Saldo tidak mencukupi, silahkan topup terlebih dahulu",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadingDialog.dismissLoading()
                } else {
                    checkout()
                }
            }

            btnCancel.setOnClickListener {
                startActivity(Intent(this@CheckoutActivity, SeatActivity::class.java))
            }
        }
    }

    private fun checkout() {
        val now = Timestamp.now()
        val transactionData = hashMapOf(
            "userId" to user.uid,
            "movieId" to movie!!.movieId,
            "seats" to seats,
            "price" to totalPrice,
            "createdAt" to now,
        )

        val docRef = db.collection("transactions").document()
        docRef.set(transactionData)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    updateBalance()
                } else {
                    Toast.makeText(
                        this,
                        it.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    loadingDialog.dismissLoading()
                }
            }
    }

    private fun updateBalance() {
       val docRef = db.collection("users").document(user.uid)
        docRef.update("balance", FieldValue.increment(-totalPrice!!.toLong()))  //mengurangi balance akun sesuai dengan harga
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    successDialog()
                } else {
                    Toast.makeText(
                        this,
                        it.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    loadingDialog.dismissLoading()
                }
            }
    }

    private fun successDialog() {
        val dialogBinding: CardTicketSuccessBinding = CardTicketSuccessBinding.inflate(this.layoutInflater)
        val dialog = Dialog(this, style.DialogTheme)

        if (dialogBinding.root.parent != null) {
            (dialogBinding.root.parent as ViewGroup).removeView(dialogBinding.root)
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.show()

        dialogBinding.btnTicket.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment_activity_home, TicketFragment())
            transaction.commit()
        }

        dialogBinding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }
    }

    override fun onStop() {
        super.onStop()
        listener.remove()
    }
}