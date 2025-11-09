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

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set flags to show on lock screen BEFORE setContentView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        
        setContentView(R.layout.activity_incoming_call)
        Log.d("IncomingCallActivity", "Activity created")

        // Initialize and start ringtone
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.ringtone)?.apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            Log.e("IncomingCallActivity", "Failed to play ringtone", e)
        }

        findViewById<Button>(R.id.acceptButton).setOnClickListener {
            stopRingtone()
            Toast.makeText(this, "Call Accepted", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.rejectButton).setOnClickListener {
            stopRingtone()
            Toast.makeText(this, "Call Rejected", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        stopRingtone()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        // Don't stop ringtone on pause, user might go back
    }
}