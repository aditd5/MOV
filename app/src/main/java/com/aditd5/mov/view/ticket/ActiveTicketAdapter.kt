package com.aditd5.mov.view.ticket

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.recyclerview.widget.RecyclerView
import com.aditd5.mov.R
import com.aditd5.mov.databinding.ItemTicketBinding
import com.aditd5.mov.databinding.QrDialogBinding
import com.aditd5.mov.model.Transaction
import com.aditd5.mov.view.transaction.TransactionActivity
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("SetTextI18n")
class ActiveTicketAdapter(private val data: List<Transaction>) : RecyclerView.Adapter<ActiveTicketAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTicketBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val binding = ItemTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        with(holder.binding) {
            with(data[position]) {
                val dateTime = this.showTime!!.toDate()
                val date = SimpleDateFormat("EEEE dd MMMM yyyy", Locale("id", "ID")).format(dateTime)
                val time = SimpleDateFormat("HH:mm", Locale("id", "ID")).format(dateTime)

                val seatString = this.seats!!.joinToString(", ")
                val passcode = this.passcode

                Picasso.get()
                    .load(this.movie!!.posterUrl)
                    .into(ivPoster)

                tvTitle.text = this.movie!!.title
                tvDate.text = date
                tvTime.text = time
                tvLocation.text = "Golden Theater, Tulungagung"
                tvSeat.text = seatString
                tvPasscode.text = passcode

                val qrgEncoder = QRGEncoder(passcode , null , QRGContents.Type.TEXT, 300)
                qrgEncoder.colorBlack = Color.WHITE
                qrgEncoder.colorWhite = Color.BLACK

                try {
                    val bitmap = qrgEncoder.getBitmap(0)
                    ivQRCode.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    Log.e(TAG , e.toString())
                }

                holder.itemView.setOnClickListener {
                    qrDialog(qrgEncoder.getBitmap(), holder.itemView.context)
                }

                holder.itemView.setOnLongClickListener(View.OnLongClickListener {
                    val context = holder.itemView.context
                    val intent = Intent(context, TransactionActivity::class.java)
                    intent.putExtra("transaction", this)
                    context.startActivity(intent)

                    return@OnLongClickListener true
                })
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun qrDialog(bitmap: Bitmap, context: Context) {
        val dialogBinding = QrDialogBinding.inflate(LayoutInflater.from(context))
        val dialog = Dialog(context, R.style.DialogTheme)

        if (dialogBinding.root.parent != null) {
            (dialogBinding.root.parent as ViewGroup).removeView(dialogBinding.root)
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        dialogBinding.ivQRCode.setImageBitmap(bitmap)

        dialog.show()

        dialogBinding.btnTicket.setOnClickListener {
            dialog.dismiss()
        }
    }
}