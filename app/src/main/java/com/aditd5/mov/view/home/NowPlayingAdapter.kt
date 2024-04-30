package com.aditd5.mov.view.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditd5.mov.databinding.RowItemNowPlayingBinding
import com.aditd5.mov.model.Film
import com.aditd5.mov.view.detail.DetailActivity
import com.squareup.picasso.Picasso

class NowPlayingAdapter(private val data: List<Film>) : RecyclerView.Adapter<NowPlayingAdapter.ViewHolder>() {

    class ViewHolder(val binding: RowItemNowPlayingBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = RowItemNowPlayingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(data[position]) {
                tvTitle.text = this.title
                tvGenre.text = this.genre
                tvRating.text = this.rating

                Picasso.get()
                    .load(this.poster)
                    .into(ivPoster)

                cardView.setOnClickListener {
                    val context = holder.itemView.context
                    val intent = Intent(context, DetailActivity::class.java)
                    intent.putExtra("title", this.title)
                    intent.putExtra("genre", this.genre)
                    intent.putExtra("rating", this.rating)
                    intent.putExtra("posterUrl", this.poster)
                    intent.putExtra("desc", this.desc)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
