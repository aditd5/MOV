package com.aditd5.mov.view.checkout

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditd5.mov.R.style
import com.aditd5.mov.databinding.ActivityCheckoutBinding
import com.aditd5.mov.databinding.CardTicketSuccessBinding
import com.aditd5.mov.model.Checkout
import com.aditd5.mov.view.home.HomeActivity
import com.aditd5.mov.view.SelectSeatActivity
import java.text.NumberFormat
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainButton()
        setData()
    }

    @SuppressLint("SetTextI18n")
    private fun setData() {
        val seats = intent.getStringArrayListExtra("seats")
        val price = 70_000
        val checkoutList: List<Checkout> = seats?.map { Checkout(it, price) } ?: emptyList()

        val checkoutAdapter = CheckoutAdapter(checkoutList)

        binding.rvCheckout.apply {
            layoutManager = LinearLayoutManager(this@CheckoutActivity)
            adapter = checkoutAdapter
        }

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        formatRupiah.maximumFractionDigits = 0

        val totalPrice = seats!!.size * price
        binding.tvTotalprice.text = formatRupiah.format(totalPrice)
    }

    private fun mainButton() {
        binding.apply {
            btnPay.setOnClickListener {
                successDialog()
            }

            btnCancel.setOnClickListener {
                startActivity(Intent(this@CheckoutActivity, SelectSeatActivity::class.java))
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

        }
        dialogBinding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}