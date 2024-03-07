package com.example.spandan_file_sender

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform