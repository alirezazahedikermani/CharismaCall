package com.example.incomingcallfcm

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "From: ${remoteMessage.from}")
        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("notifications_enabled", true)) {
            Log.d("FCM", "Notifications are disabled. Ignoring message.")
            return
        }

        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            db.callHistoryDao().insert(CallHistory(timestamp = System.currentTimeMillis()))
        }

        // Use overlay service if we have permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            val overlayIntent = Intent(this, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(overlayIntent)
            } else {
                startService(overlayIntent)
            }
        } else {
            // Fall back to notification with full-screen intent
            val serviceIntent = Intent(this, NotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }
}