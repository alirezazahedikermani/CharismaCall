package com.example.incomingcallfcm

import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast

class IncomingCallActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)
        Log.d("IncomingCallActivity", "Activity created")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

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
