package com.example.incomingcallfcm

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check permissions first
        askNotificationPermission()
        checkFullScreenIntentPermission()
        checkManageOwnCallsPermission()
        checkBatteryOptimization()
        checkSystemAlertWindowPermission()

        // Launch CallHistoryActivity as main page
        val intent = Intent(this, CallHistoryActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.canUseFullScreenIntent()) {
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs 'Full-screen intents' permission to show incoming calls on the lock screen.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:$packageName"))
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun checkManageOwnCallsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_OWN_CALLS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.MANAGE_OWN_CALLS)
            }
        }
    }

    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val packageName = packageName
            
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                AlertDialog.Builder(this)
                    .setTitle("Battery Optimization")
                    .setMessage("For reliable incoming call notifications, please disable battery optimization for this app.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to general battery settings
                            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun checkSystemAlertWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                AlertDialog.Builder(this)
                    .setTitle("Display over other apps")
                    .setMessage("This permission helps show incoming calls more reliably.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}