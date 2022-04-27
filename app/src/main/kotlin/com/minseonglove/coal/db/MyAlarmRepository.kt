package com.minseonglove.coal.db

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MyAlarmRepository @Inject constructor(
    private val dao: MyAlarmDao
) {
    fun getAll() = flow {
        dao.getAll().collect {
            emit(it)
        }
    }.catch {
        emit(listOf())
    }

    suspend fun insert(alarm: MyAlarm) {
        dao.insert(alarm)
    }

    suspend fun updateRunning(state: Boolean, id: Int) {
        dao.updateRunning(state, id)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteById(id)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}