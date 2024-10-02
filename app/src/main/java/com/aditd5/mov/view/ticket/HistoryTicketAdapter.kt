package com.aditd5.mov.view.ticket

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditd5.mov.R
import com.aditd5.mov.databinding.ItemHistoryBinding
import com.aditd5.mov.model.Transaction
import com.aditd5.mov.view.transaction.TransactionActivity
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryTicketAdapter(private val data: List<Transaction>) : RecyclerView.Adapter<HistoryTicketAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHistoryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        with(holder.binding) {
            with(data[position]) {
                val dateTime = this.showTime!!.toDate()
                val date = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.getDefault()).format(dateTime)
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(dateTime)

                Picasso.get()
                    .load(this.movie?.posterUrl)
                    .into(ivPoster)

                tvTitle.text = this.movie?.title
                tvDate.text = date
                tvTime.text = time
                tvLocation.text = "Golden Theatre, Tulungagung"
                tvTotalSeat.text = this.seats!!.size.toString() + R.string.seat

                holder.itemView.setOnClickListener {
                    val context = holder.itemView.context
                    val intent = Intent(context, TransactionActivity::class.java)
                    intent.putExtra("transaction", this)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}