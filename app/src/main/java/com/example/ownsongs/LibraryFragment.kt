package com.example.ownsongs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ownsongs.databinding.FragmentLibraryBinding

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SongAdapter

    // Sample library songs - Jamendo Royalty-Free
    private val librarySongs = listOf(
        Song("Indian Atmosphere", "SHERIFF PROJECT", "https://usercontent.jamendo.com?type=album&id=437894&width=300", "https://mp3d.jamendo.com/download/track/1880313/mp32/", "Trending"),
        Song("Indian Chill 1 Tum", "Climetree", "https://usercontent.jamendo.com?type=album&id=132717&width=300", "https://mp3d.jamendo.com/download/track/1129136/mp32/", "Trending"),
        Song("Indian Inspirations", "Christian Petermann", "https://usercontent.jamendo.com?type=album&id=315842&width=300", "https://mp3d.jamendo.com/download/track/1658691/mp32/", "Trending"),
        Song("Indian Techno 4", "Climetree", "https://usercontent.jamendo.com?type=album&id=122421&width=300", "https://mp3d.jamendo.com/download/track/1031545/mp32/", "Trending"),
        Song("Indian Techno 5", "Climetree", "https://usercontent.jamendo.com?type=album&id=122421&width=300", "https://mp3d.jamendo.com/download/track/1031546/mp32/", "Trending")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        updateUI()
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter(librarySongs, isHorizontal = false) { song, index ->
            (activity as? MainActivity)?.playSong(librarySongs, index)
        }
        binding.libraryRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.libraryRecycler.adapter = adapter
    }

    private fun updateUI() {
        if (librarySongs.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.libraryRecycler.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.libraryRecycler.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
