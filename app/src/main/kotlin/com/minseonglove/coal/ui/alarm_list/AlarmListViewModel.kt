package com.minseonglove.coal.ui.alarm_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minseonglove.coal.db.AppDatabase
import com.minseonglove.coal.db.MyAlarm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val db: AppDatabase
): ViewModel() {
    private val _testList = MutableSharedFlow<List<MyAlarm>>()

    val testList: SharedFlow<List<MyAlarm>> get() = _testList

    fun getTest() {
        viewModelScope.launch(Dispatchers.IO) {
            _testList.emit(db.myAlarmDao().getAll())
        }
    }
}
