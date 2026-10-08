package com.example.ownsongs

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ownsongs.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    private val allSongs = listOf(
        Song("Indian Atmosphere", "SHERIFF PROJECT", "https://usercontent.jamendo.com?type=album&id=437894&width=300", "https://mp3d.jamendo.com/download/track/1880313/mp32/"),
        Song("Indian Chill 1 Tum", "Climetree", "https://usercontent.jamendo.com?type=album&id=132717&width=300", "https://mp3d.jamendo.com/download/track/1129136/mp32/"),
        Song("Epic Cinematic", "Scott Holmes", "https://picsum.photos/seed/e1/500", "https://mp3d.jamendo.com/download/track/1531631/mp32/"),
        Song("Summer Breeze", "Bensound", "https://picsum.photos/seed/e2/500", "https://mp3d.jamendo.com/download/track/1531632/mp32/")
    )

    private val webSongs = listOf(
        Song("Indian Inspirations", "Christian Petermann", "https://usercontent.jamendo.com?type=album&id=315842&width=300", "https://mp3d.jamendo.com/download/track/1658691/mp32/"),
        Song("Indian Techno 4", "Climetree", "https://usercontent.jamendo.com?type=album&id=122421&width=300", "https://mp3d.jamendo.com/download/track/1031545/mp32/"),
        Song("Indian Techno 5", "Climetree", "https://usercontent.jamendo.com?type=album&id=122421&width=300", "https://mp3d.jamendo.com/download/track/1031546/mp32/")
    )

    private lateinit var adapter: SongAdapter
    private var currentSearchResults = listOf<Song>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SongAdapter(emptyList(), isHorizontal = false) { song, index ->
            (activity as? MainActivity)?.playSong(currentSearchResults, index)
        }

        binding.searchResultRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultRecycler.adapter = adapter

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String) {
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        
        if (query.isEmpty()) {
            showBrowseAll()
            return
        }

        searchRunnable = Runnable {
            fetchSongsDynamically(query)
        }
        searchHandler.postDelayed(searchRunnable!!, 500)
    }

    private fun showBrowseAll() {
        if (_binding != null) {
            binding.browseAllSection.visibility = View.VISIBLE
            binding.searchResultRecycler.visibility = View.GONE
            binding.searchProgressBar.visibility = View.GONE
            adapter.updateList(emptyList())
        }
    }

    private fun fetchSongsDynamically(query: String) {
        if (_binding != null) {
            binding.browseAllSection.visibility = View.GONE
            binding.searchResultRecycler.visibility = View.GONE
            binding.searchProgressBar.visibility = View.VISIBLE
        }

        // Simulate network delay
        searchHandler.postDelayed({
            val combinedList = allSongs + webSongs
            val filtered = combinedList.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.artist.contains(query, ignoreCase = true) 
            }
            
            if (_binding != null) {
                binding.searchProgressBar.visibility = View.GONE
                binding.searchResultRecycler.visibility = View.VISIBLE
                currentSearchResults = filtered
                adapter.updateList(filtered)
            }
        }, 500)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        _binding = null
    }
}
