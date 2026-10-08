package com.example.ownsongs

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ownsongs.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    
    private val updateProgress = object : Runnable {
        override fun run() {
            if (PlaybackManager.isPlaying()) {
                val duration = PlaybackManager.getDuration()
                if (duration > 0) {
                    val progress = (PlaybackManager.getCurrentPosition().toFloat() / duration.toFloat() * 100).toInt()
                    binding.miniPlayerProgress.progress = progress
                }
            }
            handler.postDelayed(this, 1000)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notifications are required for background playback", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupMiniPlayer()
        
        PlaybackManager.init(this)
        checkNotificationPermission()
        
        PlaybackManager.addStateChangedListener(stateListener)
        
        handler.post(updateProgress)
    }

    private val stateListener = {
        PlaybackManager.currentSong?.let { song ->
            updateMiniPlayerUI(song)
            binding.miniPlayerPlayPause.setImageResource(
                if (PlaybackManager.isPlaying()) R.drawable.ic_pause else R.drawable.ic_play
            )
        }
        Unit
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupNavigation() {
        // Initial fragment
        loadFragment(HomeFragment())

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_search -> loadFragment(SearchFragment())
                R.id.nav_library -> loadFragment(LibraryFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setupMiniPlayer() {
        binding.miniPlayerPlayPause.setOnClickListener {
            val isPlaying = PlaybackManager.togglePlayback()
            binding.miniPlayerPlayPause.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        binding.miniPlayer.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            startActivity(intent)
        }
    }

    fun playSong(songs: List<Song>, index: Int) {
        PlaybackManager.playSong(songs, index)
    }

    private fun updateMiniPlayerUI(song: Song) {
        binding.miniPlayer.visibility = View.VISIBLE
        binding.miniPlayerTitle.text = song.title
        binding.miniPlayerArtist.text = song.artist
        Glide.with(this).load(song.imageUrl).into(binding.miniPlayerImage)
    }

    override fun onResume() {
        super.onResume()
        // Sync UI if returning from PlayerActivity
        PlaybackManager.currentSong?.let { song ->
            updateMiniPlayerUI(song)
            binding.miniPlayerPlayPause.setImageResource(
                if (PlaybackManager.isPlaying()) R.drawable.ic_pause else R.drawable.ic_play
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgress)
        PlaybackManager.removeStateChangedListener(stateListener)
    }
}