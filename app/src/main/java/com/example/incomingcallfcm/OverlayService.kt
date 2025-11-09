package com.example.incomingcallfcm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Check if we have overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.e("OverlayService", "No overlay permission")
            // Fall back to launching activity
            launchActivity()
            stopSelf()
            return START_NOT_STICKY
        }

        showOverlay()
        startRingtone()

        return START_STICKY
    }

    private fun showOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_incoming_call, null)

        overlayView?.findViewById<Button>(R.id.acceptButton)?.setOnClickListener {
            stopRingtone()
            removeOverlay()
            // Handle accept call logic here
            android.widget.Toast.makeText(this, "Call Accepted", android.widget.Toast.LENGTH_SHORT).show()
        }

        overlayView?.findViewById<Button>(R.id.rejectButton)?.setOnClickListener {
            stopRingtone()
            removeOverlay()
            // Handle reject call logic here
            android.widget.Toast.makeText(this, "Call Rejected", android.widget.Toast.LENGTH_SHORT).show()
        }

        try {
            windowManager?.addView(overlayView, params)
            Log.d("OverlayService", "Overlay added")
        } catch (e: Exception) {
            Log.e("OverlayService", "Failed to add overlay", e)
            // Fall back to launching activity
            launchActivity()
            stopSelf()
        }
    }

    private fun launchActivity() {
        val activityIntent = Intent(this, IncomingCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        try {
            startActivity(activityIntent)
        } catch (e: Exception) {
            Log.e("OverlayService", "Failed to launch activity", e)
        }
    }

    private fun startRingtone() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.ringtone)?.apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            Log.e("OverlayService", "Failed to play ringtone", e)
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

    private fun removeOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
        stopSelf()
    }

    override fun onDestroy() {
        stopRingtone()
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}