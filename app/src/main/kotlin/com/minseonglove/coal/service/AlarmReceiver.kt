package com.minseonglove.coal.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("coin", "Alarm Receiver")
        if (context != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(context, RestartService::class.java).let {
                    context.startForegroundService(it)
                }
            } else {
                Intent(context, WatchIndicatorService::class.java).let {
                    context.startService(it)
                }
            }
        }
    }
}
