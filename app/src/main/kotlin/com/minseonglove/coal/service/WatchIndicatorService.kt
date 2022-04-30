package com.minseonglove.coal.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.minseonglove.coal.R
import com.minseonglove.coal.api.data.Constants.makeCoinNameString
import com.minseonglove.coal.api.data.Constants.makeConditionString
import com.minseonglove.coal.api.repository.CheckCandleRepository
import com.minseonglove.coal.db.MyAlarm
import com.minseonglove.coal.db.MyAlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class WatchIndicatorService : Service() {

    @Inject
    lateinit var checkCandleRepo: CheckCandleRepository
    @Inject
    lateinit var alarmRepo: MyAlarmRepository

    private val runningAlarm = mutableListOf<WatchIndicatorRepository>()

    private lateinit var coroutineScope: Job
    private lateinit var largeIcon: Bitmap

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        normalExit = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        service = intent

        largeIcon = BitmapFactory.decodeResource(resources, R.drawable.logo_round)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        coroutineScope = CoroutineScope(Dispatchers.IO).launch {
            alarmRepo.getAll().collectLatest { myAlarmList ->
                Log.d("coin", "변경 감지")
                initRunningAlarm()
                myAlarmList.forEach { myAlarm ->
                    if (myAlarm.isRunning) {
                        // 활성된 알람이 하나라도 있다면 죽지 않는 서비스를 가동 합니다.
                        normalExit = false
                        runningAlarm.add(
                            WatchIndicatorRepository(
                                checkCandleRepo,
                                myAlarm,
                                alarmNotify = {
                                    notificationManager.notify(
                                        Random.nextInt(),
                                        createNotification(pendingIntent, myAlarm)
                                    )
                                },
                                updateRunningState = { state, id -> updateRunning(state, id) }
                            )
                        )
                    }
                }
                runningAlarm.forEach {
                    it.getCandles()
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("coin", "Service Destroy")
        service = null
        if (::coroutineScope.isInitialized) {
            coroutineScope.cancel()
        }
        initRunningAlarm()
        if (!normalExit) {
            setAlarmTimer()
        }
    }

    private fun initRunningAlarm() {
        if (runningAlarm.isNotEmpty()) {
            runningAlarm.forEach {
                it.cancelCollect()
            }
            runningAlarm.clear()
        }
    }

    private fun setAlarmTimer() {
        // 1초 뒤에 알람을 보내도록 설정
        Calendar.getInstance().let {
            it.timeInMillis = System.currentTimeMillis()
            it.add(Calendar.SECOND, 1)
            val intent = Intent(this, AlarmReceiver::class.java)
            @SuppressLint("UnspecifiedImmutableFlag")
            val sender = PendingIntent.getBroadcast(this, 0, intent, 0)
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            // RTC_WAKEUP : 지정된 시간에 기기의 절전 모드를 해제하여 대기 중인 인텐트 실행
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, it.timeInMillis, sender)
        }
    }

    private fun createNotification(pendingIntent: PendingIntent, alarm: MyAlarm): Notification {
        val message = """
            ${makeCoinNameString(alarm.coinName, alarm.minute)}
            ${makeConditionString(
            alarm,
            resources.getStringArray(R.array.indicator_items),
            resources.getStringArray(R.array.up_down_items),
            resources.getStringArray(R.array.cross_items)
        )}
        """.trimIndent()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_logo_foreground)
            .setLargeIcon(largeIcon)
            .setContentTitle("큰일났어요!")
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateRunning(state: Boolean, id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            alarmRepo.updateRunning(state, id)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "조건 달성 완료"
            enableLights(true)
            enableVibration(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "channel_id"
        private const val CHANNEL_NAME = "channel_name"
        var service: Intent? = null
        var normalExit: Boolean = false
    }
}
