package com.aditd5.mov.view.checkout

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditd5.mov.databinding.RowItemCheckoutBinding
import com.aditd5.mov.model.Checkout
import java.text.NumberFormat
import java.util.Locale

class CheckoutAdapter(private val data: List<Checkout>) : RecyclerView.Adapter<CheckoutAdapter.ViewHolder>() {

    class ViewHolder(val binding: RowItemCheckoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = RowItemCheckoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(data[position]) {
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                formatRupiah.maximumFractionDigits = 0

                tvSeatNumber.text = "Seat no ${this.seat}"
                tvPrice.text = formatRupiah.format(this.price)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
