package com.example.ownsongs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ownsongs.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isManualClearing = false

    // Updated with unique audio URLs for each song to ensure variety
    private val allSongs = listOf(
        // Trending (Jamendo Royalty-Free)
        Song("Indian Atmosphere", "SHERIFF PROJECT", "https://usercontent.jamendo.com?type=album&id=437894&width=300", "https://mp3d.jamendo.com/download/track/1880313/mp32/", "Trending"),
        Song("Indian Chill 1 Tum", "Climetree", "https://usercontent.jamendo.com?type=album&id=132717&width=300", "https://mp3d.jamendo.com/download/track/1129136/mp32/", "Trending"),
        Song("Indian Inspirations", "Christian Petermann", "https://usercontent.jamendo.com?type=album&id=315842&width=300", "https://mp3d.jamendo.com/download/track/1658691/mp32/", "Trending"),
        Song("Indian Techno 4", "Climetree", "https://usercontent.jamendo.com?type=album&id=122421&width=300", "https://mp3d.jamendo.com/download/track/1031545/mp32/", "Trending"),
        Song("Indian Techno 5", "Climetree", "https://usercontent.jamendo.com?type=album&id=122421&width=300", "https://mp3d.jamendo.com/download/track/1031546/mp32/", "Trending"),

        // Kannada (Placeholders with real music)
        Song("Dwapara (Instrumental)", "Jithin Raj", "https://picsum.photos/seed/k1/500", "https://mp3d.jamendo.com/download/track/1880313/mp32/", "Kannada"),
        Song("Belageddu (Instrumental)", "Vijay Prakash", "https://picsum.photos/seed/k2/500", "https://mp3d.jamendo.com/download/track/1129136/mp32/", "Kannada"),
        Song("Singara Siriye (Instrumental)", "Vijay Prakash", "https://picsum.photos/seed/k3/500", "https://mp3d.jamendo.com/download/track/1658691/mp32/", "Kannada"),

        // Telugu (Placeholders with real music)
        Song("Fear Song (Instrumental)", "Anirudh", "https://picsum.photos/seed/t1/500", "https://mp3d.jamendo.com/download/track/1031545/mp32/", "Telugu"),
        Song("Pushpa Pushpa (Instrumental)", "DSP", "https://picsum.photos/seed/t2/500", "https://mp3d.jamendo.com/download/track/1031546/mp32/", "Telugu"),

        // English (Real Pop/Rock from Jamendo)
        Song("Epic Cinematic", "Scott Holmes", "https://picsum.photos/seed/e1/500", "https://mp3d.jamendo.com/download/track/1531631/mp32/", "English"),
        Song("Summer Breeze", "Bensound", "https://picsum.photos/seed/e2/500", "https://mp3d.jamendo.com/download/track/1531632/mp32/", "English")
    )

    private val recentlyPlayedList = mutableListOf<Song>()
    private var currentDisplayedSongs = listOf<Song>()
    private lateinit var songAdapter: SongAdapter
    private lateinit var recentAdapter: SongAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupListeners()
        filterSongs("") 
    }

    private fun setupAdapters() {
        songAdapter = SongAdapter(emptyList(), isHorizontal = true) { song, index ->
            addToRecentlyPlayed(song)
            (activity as? MainActivity)?.playSong(currentDisplayedSongs, index)
        }
        binding.songRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.songRecycler.adapter = songAdapter

        recentAdapter = SongAdapter(recentlyPlayedList, isHorizontal = true) { song, index ->
            (activity as? MainActivity)?.playSong(recentlyPlayedList, index)
        }
        binding.recentRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recentRecycler.adapter = recentAdapter
    }

    private fun setupListeners() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isManualClearing) {
                    filterSongs(s.toString())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.cardKannada.setOnClickListener { filterByLanguage("Kannada") }
        binding.cardTelugu.setOnClickListener { filterByLanguage("Telugu") }
        binding.cardEnglish.setOnClickListener { filterByLanguage("English") }
        binding.cardTrending.setOnClickListener { filterByLanguage("Trending") }

    }

    private fun filterSongs(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            val trending = allSongs.filter { it.language == "Trending" }
            currentDisplayedSongs = trending
            songAdapter.updateList(trending)
            binding.sectionHeader.text = "Trending on Gaana"
            binding.tvNoResults.visibility = View.GONE
            showHomeSections(true)
        } else {
            val results = allSongs.filter {
                it.title.contains(trimmedQuery, ignoreCase = true) ||
                it.artist.contains(trimmedQuery, ignoreCase = true)
            }
            currentDisplayedSongs = results
            songAdapter.updateList(results)
            binding.sectionHeader.text = "Search Results"
            binding.tvNoResults.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
            showHomeSections(false)
        }
        binding.songRecycler.scrollToPosition(0)
    }

    private fun filterByLanguage(lang: String) {
        isManualClearing = true
        binding.searchBar.setText("")
        isManualClearing = false
        
        val filtered = allSongs.filter { it.language.equals(lang, ignoreCase = true) }
        currentDisplayedSongs = filtered
        songAdapter.updateList(filtered)
        binding.sectionHeader.text = "$lang Hits"
        binding.tvNoResults.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        
        showHomeSections(true)
        binding.songRecycler.scrollToPosition(0)
    }

    private fun showHomeSections(show: Boolean) {
        val hasRecent = recentlyPlayedList.isNotEmpty()
        binding.recentHeader.visibility = if (show && hasRecent) View.VISIBLE else View.GONE
        binding.recentRecycler.visibility = if (show && hasRecent) View.VISIBLE else View.GONE
        
        binding.categoriesHeader.visibility = if (show) View.VISIBLE else View.GONE
        binding.categoriesSection.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun addToRecentlyPlayed(song: Song) {
        if (!recentlyPlayedList.contains(song)) {
            recentlyPlayedList.add(0, song)
            if (recentlyPlayedList.size > 10) recentlyPlayedList.removeAt(10)
            recentAdapter.updateList(recentlyPlayedList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
