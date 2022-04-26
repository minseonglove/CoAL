package com.minseonglove.coal.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MyAlarm::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun myAlarmDao(): MyAlarmDao
}
