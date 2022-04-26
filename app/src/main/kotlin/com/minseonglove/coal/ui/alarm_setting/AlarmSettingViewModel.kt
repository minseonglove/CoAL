package com.minseonglove.coal.ui.alarm_setting

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AlarmSettingViewModel : ViewModel() {

    private val _selectedCoin = MutableStateFlow("")

    val selectedCoin: StateFlow<String> get() = _selectedCoin

    fun setCoinName(coinName: String) {
        _selectedCoin.value = coinName
    }
}
