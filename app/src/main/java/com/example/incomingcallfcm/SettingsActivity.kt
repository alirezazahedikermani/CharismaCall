package com.example.incomingcallfcm

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.messaging.FirebaseMessaging

class SettingsActivity : AppCompatActivity() {

    private lateinit var notificationSwitch: SwitchMaterial
    private lateinit var showTokenButton: Button
    private lateinit var fcmTokenTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        notificationSwitch = findViewById(R.id.notificationSwitch)
        showTokenButton = findViewById(R.id.showTokenButton)
        fcmTokenTextView = findViewById(R.id.fcmToken)

        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        notificationSwitch.isChecked = sharedPreferences.getBoolean("notifications_enabled", true)

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply()
        }

        showTokenButton.setOnClickListener {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                fcmTokenTextView.text = token
                Log.d("FCM", "FCM token: $token")
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("FCM Token", token)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "FCM Token copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
