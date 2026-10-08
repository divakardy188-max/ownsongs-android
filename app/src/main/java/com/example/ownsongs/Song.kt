package com.example.ownsongs

import java.io.Serializable

data class Song(
    val title: String,
    val artist: String,
    val imageUrl: String,
    val audioUrl: String,
    val language: String = "English"
) : Serializable
