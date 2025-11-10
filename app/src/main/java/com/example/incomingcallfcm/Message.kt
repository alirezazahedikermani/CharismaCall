package com.example.incomingcallfcm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val subject: String,
    val body: String,
    val action: String, // "call", "notification", or other
    val timestamp: Long,
    val isRead: Boolean = false
)
