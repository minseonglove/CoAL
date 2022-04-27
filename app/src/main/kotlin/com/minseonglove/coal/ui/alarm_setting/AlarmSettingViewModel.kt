package com.minseonglove.coal.ui.alarm_setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minseonglove.coal.db.AppDatabase
import com.minseonglove.coal.db.MyAlarm
import com.minseonglove.coal.db.MyAlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmSettingViewModel @Inject constructor(
    private val repository: MyAlarmRepository
): ViewModel() {

    private val _selectedCoin = MutableStateFlow("")

    val selectedCoin: StateFlow<String> get() = _selectedCoin

    fun setCoinName(coinName: String) {
        _selectedCoin.value = coinName
    }

    fun addAlarm(alarm: MyAlarm) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(alarm)
        }
    }
}
