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
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat

class NotificationService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "incoming_call"
        private const val WAKE_LOCK_TAG = "CharismaCall:IncomingCall"
    }

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        // Acquire wake lock to ensure device wakes up
        acquireWakeLock()
        // Create notification immediately in onCreate
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("NotificationService", "Service started")

        val name = intent?.getStringExtra("name") ?: "Incoming Call"
        val subject = intent?.getStringExtra("subject") ?: ""

        // Start foreground with full-screen notification
        startForeground(NOTIFICATION_ID, createNotification(name, subject))

        // Auto-stop service after 30 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            stopSelf()
        }, 30000)

        return START_STICKY
    }

    private fun createNotification(name: String = "Incoming Call", subject: String = ""): android.app.Notification {
        val activityIntent = Intent(this, IncomingCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                     Intent.FLAG_ACTIVITY_CLEAR_TOP or
                     Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            putExtra("from_notification", true)
            putExtra("name", name)
            putExtra("subject", subject)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(), // Unique request code
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val displayText = if (subject.isNotEmpty()) "$name - $subject" else name

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(displayText)
            .setContentText("Incoming call")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setTimeoutAfter(30000) // Auto dismiss after 30 seconds
            .build()
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
                WAKE_LOCK_TAG
            ).apply {
                acquire(30000) // 30 seconds
            }
            Log.d("NotificationService", "Wake lock acquired")
        } catch (e: Exception) {
            Log.e("NotificationService", "Failed to acquire wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d("NotificationService", "Wake lock released")
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e("NotificationService", "Failed to release wake lock", e)
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
                description = "Full-screen notifications for incoming calls"
                setSound(null, null) // Sound is handled by the activity
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationService", "Notification channel created with HIGH importance")
        }
    }

    override fun onDestroy() {
        Log.d("NotificationService", "Service destroyed")
        releaseWakeLock()
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