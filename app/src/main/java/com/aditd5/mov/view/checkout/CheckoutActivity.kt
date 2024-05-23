@file:Suppress("DEPRECATION")

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
import com.aditd5.mov.R.style
import com.aditd5.mov.databinding.ActivityCheckoutBinding
import com.aditd5.mov.databinding.CardTicketSuccessBinding
import com.aditd5.mov.model.Checkout
import com.aditd5.mov.model.Movie
import com.aditd5.mov.util.CurrencyFormatter
import com.aditd5.mov.util.LoadingDialog
import com.aditd5.mov.view.SelectSeatActivity
import com.aditd5.mov.view.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant.now

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var db: FirebaseFirestore

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

        mainButton()
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
        docRef.get()
            .addOnSuccessListener {
                val isActive = it.get("wallet")
                if (isActive == true) {
                    val mBalance = it.get("balance")
                    balance = mBalance.toString().toInt()
                    binding.tvBalance.text = CurrencyFormatter().numberFormat(balance!!)
                }
            }
    }

    private fun mainButton() {
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
//                    Toast.makeText(
//                        this@CheckoutActivity,
//                        seats.toString(),
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            }

            btnCancel.setOnClickListener {
                startActivity(Intent(this@CheckoutActivity, SelectSeatActivity::class.java))
            }
        }
    }

    private fun checkout() {
        val transactionData = hashMapOf(
            "Uid" to user.uid,
            "transactionDate" to now(),
            "seats" to listOf(seats.toList()) ,
            "movie" to movie!!.title,
            "price" to totalPrice
        )

        val docRef = db.collection("transactions").document()
        docRef.set(transactionData)
            .addOnSuccessListener {
                successDialog()
                loadingDialog.dismissLoading()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    it.message,
                    Toast.LENGTH_SHORT
                ).show()
                loadingDialog.dismissLoading()
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

        }
        dialogBinding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}