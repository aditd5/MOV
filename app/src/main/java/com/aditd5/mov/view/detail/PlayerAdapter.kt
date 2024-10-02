package com.aditd5.mov.view.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditd5.mov.databinding.ItemPlayerBinding
import com.squareup.picasso.Picasso

class PlayerAdapter(private val actors: List<Map<String, String>>) : RecyclerView.Adapter<PlayerAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPlayerBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(actors[position]) {
                tvPlayername.text = this["name"]

                Picasso.get()
                    .load(this["photoUrl"])
                    .into(ivPlayer)
            }
        }
    }

    override fun getItemCount(): Int {
        return actors.size
    }
}
