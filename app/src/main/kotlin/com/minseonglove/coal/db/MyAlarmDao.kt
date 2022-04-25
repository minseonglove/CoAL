package com.minseonglove.coal.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MyAlarmDao {
    @Query("SELECT * FROM MyAlarm")
    fun getAll(): List<MyAlarm>

    @Insert
    fun insert(myAlarm: MyAlarm)

    @Query("DELETE FROM MyAlarm WHERE id = :id")
    fun deleteById(id: Int)
}