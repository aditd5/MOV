package com.aditd5.mov.view.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditd5.mov.databinding.RowItemPlayerBinding
import com.aditd5.mov.model.Player
import com.squareup.picasso.Picasso

class PlayerAdapter(private val data: List<Player>) : RecyclerView.Adapter<PlayerAdapter.ViewHolder>() {

    class ViewHolder(val binding: RowItemPlayerBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = RowItemPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(data[position]) {
                tvPlayername.text = this.name

                Picasso.get()
                    .load(this.photo)
                    .into(ivPlayer)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
