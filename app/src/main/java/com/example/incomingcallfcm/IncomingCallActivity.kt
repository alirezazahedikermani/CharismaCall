package com.example.incomingcallfcm

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class IncomingCallActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        findViewById<Button>(R.id.acceptButton).setOnClickListener {
            mediaPlayer.stop()
            Toast.makeText(this, "Call Accepted", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.rejectButton).setOnClickListener {
            mediaPlayer.stop()
            Toast.makeText(this, "Call Rejected", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
