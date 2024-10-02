package com.aditd5.mov.view.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aditd5.mov.databinding.ItemPosterBinding
import com.aditd5.mov.model.Movie
import com.aditd5.mov.view.detail.DetailActivity
import com.squareup.picasso.Picasso

class MovieAdapter(private val data: List<Movie> , private val status: String) : RecyclerView.Adapter<MovieAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPosterBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemPosterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(data[position]) {
                Picasso.get()
                    .load(this.posterUrl)
                    .into(ivPoster)

                cardView.setOnClickListener {
                    val movie = Movie(
                        movieId = this.movieId ,
                        title = this.title ,
                        genres = this.genres ,
                        runtime = this.runtime ,
                        rating = this.rating ,
                        synopsis = this.synopsis ,
                        posterUrl = this.posterUrl ,
                        price = this.price ,
                        lastScreeningDate = this.lastScreeningDate ,
                        cast = this.cast ,
                    )
                    val context = holder.itemView.context
                    val intent = Intent(context , DetailActivity::class.java).apply {
                        putExtra("movie" , movie)
                        putExtra("status" , status)
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
