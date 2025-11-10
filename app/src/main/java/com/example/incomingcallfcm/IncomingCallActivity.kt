package com.example.incomingcallfcm

import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class IncomingCallActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private val autoCloseHandler = Handler(Looper.getMainLooper())
    private val autoCloseRunnable = Runnable {
        if (!isFinishing) {
            stopRingtone()
            Toast.makeText(this, "Call Missed", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

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

        // Get name and subject from intent
        val name = intent.getStringExtra("name") ?: "Incoming Call"
        val subject = intent.getStringExtra("subject") ?: ""

        // Update UI with name and subject
        findViewById<TextView>(R.id.callerNameText)?.text = name
        if (subject.isNotEmpty()) {
            findViewById<TextView>(R.id.callSubjectText)?.apply {
                text = subject
                visibility = android.view.View.VISIBLE
            }
        }

        // Initialize and start ringtone
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.ringtone)?.apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            Log.e("IncomingCallActivity", "Failed to play ringtone", e)
        }

        // Auto-close after 30 seconds
        autoCloseHandler.postDelayed(autoCloseRunnable, 30000)

        findViewById<Button>(R.id.acceptButton).setOnClickListener {
            autoCloseHandler.removeCallbacks(autoCloseRunnable)
            stopRingtone()
            Toast.makeText(this, "Call Accepted", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.rejectButton).setOnClickListener {
            autoCloseHandler.removeCallbacks(autoCloseRunnable)
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
        autoCloseHandler.removeCallbacks(autoCloseRunnable)
        stopRingtone()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        // Don't stop ringtone on pause, user might go back
    }
}