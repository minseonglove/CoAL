package com.minseonglove.coal.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MyAlarmDao {
    @Query("SELECT * FROM MyAlarm")
    fun getAll(): Flow<List<MyAlarm>>

    @Insert
    suspend fun insert(myAlarm: MyAlarm)

    @Query("UPDATE MyAlarm SET is_running = :state WHERE id = :id")
    suspend fun updateRunning(state: Boolean, id: Int)

    @Query("DELETE FROM MyAlarm WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM MyAlarm")
    suspend fun deleteAll()
}
