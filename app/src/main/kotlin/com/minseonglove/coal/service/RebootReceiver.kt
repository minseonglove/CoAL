package com.minseonglove.coal.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null &&
            intent != null &&
            intent.action == Intent.ACTION_BOOT_COMPLETED
        ) {
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
