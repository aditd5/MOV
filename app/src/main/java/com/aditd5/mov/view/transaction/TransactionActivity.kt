package com.aditd5.mov.view.transaction

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditd5.mov.databinding.ActivityTransactionBinding
import com.aditd5.mov.model.Checkout
import com.aditd5.mov.model.Transaction
import com.aditd5.mov.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("DEPRECATION")
class TransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionBinding

    private var data: Transaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("transaction", Transaction::class.java)
        } else {
            intent.getParcelableExtra("transaction")
        }

        setData()
    }

    private fun setData() {
        binding.apply {
            tvMovie.text = data!!.movie!!.title
            tvId.text = data!!.orderId

            val transactionList : List<Checkout> = data!!.seats!!.map { Checkout(it, data!!.movie!!.price) }
            val transactionAdapter = TransactionAdapter(transactionList)
            binding.rvTransaction.apply {
                layoutManager = LinearLayoutManager(this@TransactionActivity)
                adapter = transactionAdapter
            }

            tvTotalprice.text = CurrencyFormatter().formatRupiah(data!!.price!!.toInt())

            val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(data!!.createdAt!!.toDate())
            tvDate.text = dateFormat

            tvMethod.text = data!!.paymentMethod
        }
    }
}