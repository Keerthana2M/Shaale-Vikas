package com.example.androidappdevelopmentusinggenai_shaale_vikas.model

data class Message(
    val id: String = "",
    val senderName: String = "",
    val text: String = "",
    val needId: String? = null, // Null for global chat, ID for issue-specific chat
    val timestamp: Long = System.currentTimeMillis()
)
