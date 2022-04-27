package com.minseonglove.coal.ui.alarm_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minseonglove.coal.db.MyAlarm
import com.minseonglove.coal.db.MyAlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val repository: MyAlarmRepository
) : ViewModel() {

    val getAlarmList: SharedFlow<List<MyAlarm>> =
        repository.getAll().shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    fun updateRunningState(state: Boolean, id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRunning(state, id)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }
}
