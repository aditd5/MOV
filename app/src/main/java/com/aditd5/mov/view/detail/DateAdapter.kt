package com.aditd5.mov.view.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditd5.mov.databinding.ItemDateBinding

class DateAdapter(private val data: List<Triple<String, String, String>>, private val onClick: (Triple<String, String, String>) -> Unit) : RecyclerView.Adapter<DateAdapter.ViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    class ViewHolder(val binding: ItemDateBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val binding = ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        with(holder.binding) {
            with(data[position]) {
                tvDay.text = this.first
                tvDate.text = this.second
                tvTimestamp.text = this.third

                dateContainer.isSelected = holder.adapterPosition == selectedPosition

                dateContainer.setOnClickListener {
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