package com.aditd5.mov.view.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditd5.mov.databinding.ItemTimeBinding

class TimeAdapter(private val data: List<String>, private val onClick: (String) -> Unit) : RecyclerView.Adapter<TimeAdapter.ViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    class ViewHolder(val binding: ItemTimeBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val binding = ItemTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        with(holder.binding) {
            with(data[position]) {
                tvTime.text = this

                timeContainer.isSelected = holder.adapterPosition == selectedPosition

                timeContainer.setOnClickListener {
                    if (selectedPosition != holder.adapterPosition) {
                        selectedPosition = holder.adapterPosition
                    }

                    notifyDataSetChanged()
                    onClick(this)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}