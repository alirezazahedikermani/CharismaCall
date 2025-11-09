package com.example.incomingcallfcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

class NotificationService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "incoming_call"
    }

    override fun onCreate() {
        super.onCreate()
        // Create notification immediately in onCreate
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("NotificationService", "Service started")
        
        val activityIntent = Intent(this, IncomingCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                     Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                     Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                     Intent.FLAG_ACTIVITY_NO_HISTORY)
            putExtra("from_notification", true)
        }
        
        // Try multiple times to launch the activity
        launchActivityWithRetries(activityIntent)
        
        // Auto-stop service after 60 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            stopSelf()
        }, 60000)
        
        return START_STICKY
    }

    private fun createNotification(): android.app.Notification {
        val activityIntent = Intent(this, IncomingCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                     Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                     Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                     Intent.FLAG_ACTIVITY_NO_HISTORY)
            putExtra("from_notification", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0,
            activityIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Incoming Call")
            .setContentText("Tap to answer")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setTimeoutAfter(60000) // Auto dismiss after 60 seconds
            .build()
    }

    private fun launchActivityWithRetries(intent: Intent) {
        // Try immediately
        tryLaunchActivity(intent)
        
        // Try again after 500ms
        Handler(Looper.getMainLooper()).postDelayed({
            tryLaunchActivity(intent)
        }, 500)
        
        // Try again after 1000ms
        Handler(Looper.getMainLooper()).postDelayed({
            tryLaunchActivity(intent)
        }, 1000)
    }

    private fun tryLaunchActivity(intent: Intent) {
        try {
            startActivity(intent)
            Log.d("NotificationService", "Activity launched")
        } catch (e: Exception) {
            Log.e("NotificationService", "Failed to launch activity", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Incoming Call",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming call notifications"
                setSound(null, null)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        Log.d("NotificationService", "Service destroyed")
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}