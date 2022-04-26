package com.minseonglove.coal.ui.setting_condition

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _minute = MutableStateFlow(0)
    private val _indicator = MutableStateFlow(0)
    private val _valueCondition = MutableStateFlow(0)
    private val _signalCondition = MutableStateFlow(0)

    val minute: StateFlow<Int> get() = _minute
    val indicator: StateFlow<Int> get() = _indicator
    val valueCondition: StateFlow<Int> get() = _valueCondition
    val signalCondition: StateFlow<Int> get() = _signalCondition

    fun setSpinner(type: Int, pos: Int) {
        viewModelScope.launch {
            Log.d("coin", pos.toString())
            when (type) {
                0 -> _minute.emit(pos)
                1 -> _indicator.emit(pos)
                2 -> _valueCondition.emit(pos)
                3 -> _signalCondition.emit(pos)
            }
        }
    }
}
