package com.minseonglove.coal.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import java.util.Calendar

class WatchService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        normalExit = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        service = intent
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!normalExit) {
            setAlarmTimer()
        }
    }

    private fun setAlarmTimer() {
        // 1초 뒤에 알람을 보내도록 설정
        Calendar.getInstance().let{
            it.timeInMillis = System.currentTimeMillis()
            it.add(Calendar.SECOND, 1)
            val intent = Intent(this, AlarmReceiver::class.java)
            val sender = PendingIntent.getBroadcast(this, 0, intent, 0)
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            // RTC_WAKEUP : 지정된 시간에 기기의 절전 모드를 해제하여 대기 중인 인텐트 실행
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, it.timeInMillis, sender)
        }
    }

    companion object {
        var service: Intent? = null
        var normalExit: Boolean = false
    }
}