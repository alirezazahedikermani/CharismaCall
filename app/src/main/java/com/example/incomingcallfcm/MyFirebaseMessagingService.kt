package com.example.incomingcallfcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "message_notifications"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "From: ${remoteMessage.from}")
        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("notifications_enabled", true)) {
            Log.d("FCM", "Notifications are disabled. Ignoring message.")
            return
        }

        // Extract message data
        val data = remoteMessage.data
        val name = data["name"] ?: "Unknown"
        val subject = data["subject"] ?: "No Subject"
        val body = data["body"] ?: ""
        val action = data["action"] ?: "other"

        Log.d("FCM", "Message - name: $name, subject: $subject, action: $action")

        // Save message to database
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val message = Message(
                name = name,
                subject = subject,
                body = body,
                action = action,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            db.messageDao().insert(message)
        }

        // Route based on action type
        when (action.lowercase()) {
            "call" -> {
                // Show incoming call activity
                handleCallAction(name, subject)
            }
            "notification" -> {
                // Show notification only
                handleNotificationAction(name, subject)
            }
            else -> {
                // Just save to database, no UI action
                Log.d("FCM", "Action '$action' - message saved to database only")
            }
        }
    }

    private fun handleCallAction(name: String, subject: String) {
        // Use overlay service if we have permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            val overlayIntent = Intent(this, OverlayService::class.java).apply {
                putExtra("name", name)
                putExtra("subject", subject)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(overlayIntent)
            } else {
                startService(overlayIntent)
            }
        } else {
            // Fall back to notification with full-screen intent
            val serviceIntent = Intent(this, NotificationService::class.java).apply {
                putExtra("name", name)
                putExtra("subject", subject)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }

    private fun handleNotificationAction(name: String, subject: String) {
        createNotificationChannel()

        val intent = Intent(this, CallHistoryActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(name)
            .setContentText(subject)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Message Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming messages"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}