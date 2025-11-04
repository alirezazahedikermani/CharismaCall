package com.example.incomingcallfcm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CallHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long
)
