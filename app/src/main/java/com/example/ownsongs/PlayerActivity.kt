package com.example.ownsongs

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.ownsongs.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private val handler = Handler(Looper.getMainLooper())
    
    private val updateSeekBar = object : Runnable {
        override fun run() {
            if (PlaybackManager.isPlaying()) {
                val currentPos = PlaybackManager.getCurrentPosition()
                binding.playerSeekBar.progress = currentPos
                binding.tvCurrentTime.text = formatTime(currentPos)
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        PlaybackManager.addStateChangedListener(stateListener)
        handler.post(updateSeekBar)
    }

    private val stateListener = {
        updatePlayPauseIcon()
        val song = PlaybackManager.currentSong
        if (song != null) {
            binding.playerTitle.text = song.title
            binding.playerArtist.text = song.artist
            Glide.with(this).load(song.imageUrl).into(binding.playerImage)
        }
        Unit
    }

    private fun setupUI() {
        val song = PlaybackManager.currentSong ?: run {
            finish()
            return
        }

        binding.playerTitle.text = song.title
        binding.playerArtist.text = song.artist
        Glide.with(this).load(song.imageUrl).into(binding.playerImage)

        binding.playerSeekBar.max = PlaybackManager.getDuration()
        binding.playerSeekBar.progress = PlaybackManager.getCurrentPosition()
        binding.tvTotalTime.text = formatTime(PlaybackManager.getDuration())
        binding.tvCurrentTime.text = formatTime(PlaybackManager.getCurrentPosition())

        updatePlayPauseIcon()

        binding.btnPlayPause.setOnClickListener {
            PlaybackManager.togglePlayback()
            updatePlayPauseIcon()
        }

        binding.btnNext.setOnClickListener {
            PlaybackManager.playNext()
        }

        binding.btnPrev.setOnClickListener {
            PlaybackManager.playPrevious()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.tvCurrentTime.text = formatTime(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let { PlaybackManager.seekTo(it.progress) }
            }
        })
    }

    private fun updatePlayPauseIcon() {
        binding.btnPlayPause.setImageResource(
            if (PlaybackManager.isPlaying()) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBar)
        PlaybackManager.removeStateChangedListener(stateListener)
    }
}
