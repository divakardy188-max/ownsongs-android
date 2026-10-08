package com.example.ownsongs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ownsongs.databinding.SongItemBinding
import com.example.ownsongs.databinding.SearchResultItemBinding

class SongAdapter(
    private var songs: List<Song>,
    private val isHorizontal: Boolean = true,
    private val onSongClick: (Song, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class HorizontalViewHolder(val binding: SongItemBinding) : RecyclerView.ViewHolder(binding.root)
    class VerticalViewHolder(val binding: SearchResultItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (isHorizontal) {
            HorizontalViewHolder(SongItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            VerticalViewHolder(SearchResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val song = songs[position]
        if (holder is HorizontalViewHolder) {
            holder.binding.apply {
                songTitle.text = song.title
                songArtist.text = song.artist
                Glide.with(root.context)
                    .load(song.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(songImage)
                root.setOnClickListener { onSongClick(song, holder.adapterPosition) }
            }
        } else if (holder is VerticalViewHolder) {
            holder.binding.apply {
                songTitle.text = song.title
                songArtist.text = song.artist
                Glide.with(root.context)
                    .load(song.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(songImage)
                root.setOnClickListener { onSongClick(song, holder.adapterPosition) }
            }
        }
    }

    override fun getItemCount(): Int = songs.size

    fun updateList(newList: List<Song>) {
        songs = newList
        notifyDataSetChanged()
    }
}
