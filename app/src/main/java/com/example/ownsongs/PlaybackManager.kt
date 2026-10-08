package com.example.ownsongs

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

object PlaybackManager {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    
    var currentSong: Song? = null
        private set
    
    private var playlist: List<Song> = emptyList()

    private val stateListeners = mutableListOf<() -> Unit>()

    fun init(context: Context) {
        if (mediaController != null) return

        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    notifyListeners()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    notifyListeners()
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    android.util.Log.e("PlaybackManager", "Player error: ${error.message}", error)
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateCurrentSongFromController()
                    notifyListeners()
                }
            })
            
            // Initial sync
            updateCurrentSongFromController()
            notifyListeners()
        }, MoreExecutors.directExecutor())
    }

    private fun updateCurrentSongFromController() {
        val controller = mediaController ?: return
        val currentItem = controller.currentMediaItem
        if (currentItem != null) {
            val metadata = currentItem.mediaMetadata
            currentSong = Song(
                title = metadata.title?.toString() ?: "Unknown",
                artist = metadata.artist?.toString() ?: "Unknown",
                imageUrl = metadata.artworkUri?.toString() ?: "",
                audioUrl = currentItem.mediaId
            )
        }
    }

    private fun notifyListeners() {
        // Run on main thread to be safe for UI
        stateListeners.toList().forEach { it.invoke() }
    }

    fun playSong(songs: List<Song>, index: Int) {
        playlist = songs
        mediaController?.let { controller ->
            controller.clearMediaItems()
            val mediaItems = songs.map { song ->
                MediaItem.Builder()
                    .setMediaId(song.audioUrl) // Use URL as ID for easy recovery
                    .setUri(song.audioUrl)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setArtworkUri(android.net.Uri.parse(song.imageUrl))
                            .build()
                    )
                    .build()
            }
            
            controller.setMediaItems(mediaItems, index, 0L)
            controller.prepare()
            controller.play()
            
            updateCurrentSongFromController()
            notifyListeners()
        }
    }

    fun playNext() {
        mediaController?.seekToNext()
    }

    fun playPrevious() {
        mediaController?.seekToPrevious()
    }

    fun togglePlayback(): Boolean {
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
                return false
            } else {
                it.play()
                return true
            }
        }
        return false
    }

    fun isPlaying(): Boolean = mediaController?.isPlaying ?: false

    fun getDuration(): Int = mediaController?.duration?.toInt()?.coerceAtLeast(0) ?: 0

    fun getCurrentPosition(): Int = mediaController?.currentPosition?.toInt()?.coerceAtLeast(0) ?: 0

    fun seekTo(position: Int) {
        mediaController?.seekTo(position.toLong())
    }

    fun addStateChangedListener(listener: () -> Unit) {
        if (!stateListeners.contains(listener)) {
            stateListeners.add(listener)
        }
        // Notify immediately if we already have a controller
        if (mediaController != null) {
            listener.invoke()
        }
    }

    fun removeStateChangedListener(listener: () -> Unit) {
        stateListeners.remove(listener)
    }

    fun release() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        mediaController = null
    }
}
