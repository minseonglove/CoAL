package com.minseonglove.coal.ui.setting_condition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minseonglove.coal.db.MyAlarm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingConditionViewModel : ViewModel() {

    val candle = MutableStateFlow<String?>(null)
    val stochasticK = MutableStateFlow<String?>(null)
    val stochasticD = MutableStateFlow<String?>(null)
    val macdM = MutableStateFlow<String?>(null)
    val limitValue = MutableStateFlow<String?>(null)
    val signal = MutableStateFlow<String?>(null)

    private val _minutePos = MutableStateFlow(0)
    private val _indicator = MutableStateFlow(0)
    private val _valueCondition = MutableStateFlow(0)
    private val _signalCondition = MutableStateFlow(0)

    val minutePos: StateFlow<Int> get() = _minutePos
    val indicator: StateFlow<Int> get() = _indicator
    val valueCondition: StateFlow<Int> get() = _valueCondition
    val signalCondition: StateFlow<Int> get() = _signalCondition

    fun setSpinner(type: Int, pos: Int) {
        viewModelScope.launch {
            when (type) {
                0 -> _minutePos.emit(pos)
                1 -> _indicator.emit(pos)
                2 -> _valueCondition.emit(pos)
                3 -> _signalCondition.emit(pos)
            }
        }
    }

    fun getAlarm(coinName: String, minute: Int): MyAlarm =
        MyAlarm(
            0,
            coinName,
            minute,
            indicator.value,
            candle.value?.toInt(),
            stochasticK.value?.toInt(),
            stochasticD.value?.toInt(),
            macdM.value?.toInt(),
            limitValue.value?.toDouble(),
            valueCondition.value,
            signal.value?.toInt(),
            signalCondition.value,
            true
        )
}
