package com.aditd5.mov.view.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditd5.mov.databinding.RowItemMoviePosterBinding
import com.aditd5.mov.model.Movie
import com.aditd5.mov.view.detail.DetailActivity
import com.squareup.picasso.Picasso

class MovieAdapter(private val data: List<Movie> , private val status: String) : RecyclerView.Adapter<MovieAdapter.ViewHolder>() {

    class ViewHolder(val binding: RowItemMoviePosterBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = RowItemMoviePosterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(data[position]) {
                tvTitle.text = this.title

                Picasso.get()
                    .load(this.posterUrl)
                    .into(ivPoster)

                cardView.setOnClickListener {
                    val movie = Movie(title = this.title, synopsis = this.synopsis, genre = this.genre, rating = this.rating, price = this.price, posterUrl = this.posterUrl, actors = this.actors)
                    val context = holder.itemView.context
                    val intent = Intent(context, DetailActivity::class.java).apply {
                        putExtra("movie", movie)
                        putExtra("status", status)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
