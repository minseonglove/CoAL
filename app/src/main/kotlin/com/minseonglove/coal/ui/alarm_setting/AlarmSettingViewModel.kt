package com.minseonglove.coal.ui.alarm_setting

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class AlarmSettingViewModel : ViewModel() {

    val selectedCoin = MutableStateFlow("")

    fun setCoinName(coinName: String) {
        selectedCoin.value = coinName
    }
}
