package com.minseonglove.coal.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.minseonglove.coal.MainActivity
import com.minseonglove.coal.R

class RestartService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            0)

        val builder = NotificationCompat.Builder(this, "default").apply {
            setSmallIcon(R.mipmap.ic_logo_foreground)
            setContentTitle(null)
            setContentText(null)
            setContentIntent(pendingIntent)
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    "default",
                    "기본 채널",
                    NotificationManager.IMPORTANCE_NONE
                )
            )
        }

        startForeground(9, builder.build())

        startService(Intent(this, WatchService::class.java))
        stopForeground(true)
        stopSelf()

        return START_NOT_STICKY
    }
}