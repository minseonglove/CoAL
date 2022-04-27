package com.minseonglove.coal.ui.alarm_list

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
): ViewModel() {

    val testList: SharedFlow<List<MyAlarm>> =
        repository.getAll().shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }

}
