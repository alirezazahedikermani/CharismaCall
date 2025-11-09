package com.example.incomingcallfcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "overlay_service"
    }

    override fun onCreate() {
        super.onCreate()
        // Start as foreground immediately
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("OverlayService", "Service started")
        
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

    private fun createNotification(): Notification {
        createNotificationChannel()
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Incoming Call")
            .setContentText("Processing call...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service for displaying incoming call overlay"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showOverlay() {
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.CENTER

            overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_incoming_call, null)

            overlayView?.findViewById<Button>(R.id.acceptButton)?.setOnClickListener {
                Log.d("OverlayService", "Call accepted")
                stopRingtone()
                removeOverlay()
                android.widget.Toast.makeText(this, "Call Accepted", android.widget.Toast.LENGTH_SHORT).show()
            }

            overlayView?.findViewById<Button>(R.id.rejectButton)?.setOnClickListener {
                Log.d("OverlayService", "Call rejected")
                stopRingtone()
                removeOverlay()
                android.widget.Toast.makeText(this, "Call Rejected", android.widget.Toast.LENGTH_SHORT).show()
            }

            windowManager?.addView(overlayView, params)
            Log.d("OverlayService", "Overlay added successfully")
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
            Log.d("OverlayService", "Activity launched as fallback")
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
            Log.d("OverlayService", "Ringtone started")
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
        Log.d("OverlayService", "Ringtone stopped")
    }

    private fun removeOverlay() {
        try {
            overlayView?.let {
                windowManager?.removeView(it)
                overlayView = null
            }
            Log.d("OverlayService", "Overlay removed")
        } catch (e: Exception) {
            Log.e("OverlayService", "Failed to remove overlay", e)
        }
        stopSelf()
    }

    override fun onDestroy() {
        Log.d("OverlayService", "Service destroyed")
        stopRingtone()
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}