package com.example.incomingcallfcm

import android.content.Context
import android.content.Intent
import android.os.Build
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

        // Launch the activity directly first (works when app is in foreground or recent)
        val activityIntent = Intent(this, IncomingCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("from_fcm", true)
        }
        
        try {
            startActivity(activityIntent)
            Log.d("FCM", "Activity launched directly")
        } catch (e: Exception) {
            Log.e("FCM", "Failed to launch activity directly", e)
        }

        // Always start the notification service as backup
        val serviceIntent = Intent(this, NotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}