package com.minseonglove.coal.ui.alarm.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minseonglove.coal.api.data.Constants.MACD
import com.minseonglove.coal.api.data.Constants.MOVING_AVERAGE
import com.minseonglove.coal.api.data.Constants.PRICE
import com.minseonglove.coal.api.data.Constants.RSI
import com.minseonglove.coal.api.data.Constants.STOCHASTIC
import com.minseonglove.coal.db.MyAlarm
import com.minseonglove.coal.ui.coin.search.CoinSearchDto
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

    private val _priceVisible = MutableStateFlow(8)
    private val _candleVisible = MutableStateFlow(8)
    private val _maVisible = MutableStateFlow(8)
    private val _stochasticVisible = MutableStateFlow(8)
    private val _macdVisible = MutableStateFlow(8)
    private val _valueVisible = MutableStateFlow(8)
    private val _signalVisible = MutableStateFlow(8)
    private val _minutePos = MutableStateFlow(0)
    private val _indicator = MutableStateFlow(0)
    private val _valueCondition = MutableStateFlow(0)
    private val _signalCondition = MutableStateFlow(0)

    val priceVisible: StateFlow<Int> get() = _priceVisible
    val candleVisible: StateFlow<Int> get() = _candleVisible
    val maVisible: StateFlow<Int> get() = _maVisible
    val stochasticVisible: StateFlow<Int> get() = _stochasticVisible
    val macdVisible: StateFlow<Int> get() = _macdVisible
    val valueVisible: StateFlow<Int> get() = _valueVisible
    val signalVisible: StateFlow<Int> get() = _signalVisible
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

    fun getVisible() {
        viewModelScope.launch {
            shutdownAll()
            when (indicator.value) {
                PRICE -> {
                    _priceVisible.emit(0)
                }
                MOVING_AVERAGE -> {
                    _candleVisible.emit(0)
                    _maVisible.emit(0)
                }
                RSI -> {
                    _candleVisible.emit(0)
                    _valueVisible.emit(0)
                    _maVisible.emit(4)
                    _signalVisible.emit(0)
                }
                STOCHASTIC -> {
                    _stochasticVisible.emit(0)
                    _valueVisible.emit(0)
                }
                MACD -> {
                    _macdVisible.emit(0)
                    _valueVisible.emit(0)
                    _signalVisible.emit(0)
                }
            }
        }
    }

    private suspend fun shutdownAll() {
        _priceVisible.emit(8)
        _candleVisible.emit(8)
        _maVisible.emit(8)
        _stochasticVisible.emit(8)
        _macdVisible.emit(8)
        _valueVisible.emit(8)
        _signalVisible.emit(8)
        _signalCondition.emit(0)
        candle.emit(null)
        signal.emit(null)
        limitValue.emit(null)
        stochasticK.emit(null)
        stochasticD.emit(null)
        macdM.emit(null)
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

    fun validateCondition(): String {
        // 값이 올바르게 들어갔는지
        if (indicator.value != MOVING_AVERAGE) {
            val validationValue = limitValue.value?.toDoubleOrNull() ?: -1.0
            if (validationValue <= 0 && indicator.value != MACD) {
                return when (indicator.value) {
                    PRICE -> "올바른 가격을 입력해주세요!"
                    else -> "올바른 value를 입력해주세요!"
                }
            }
            if (validationValue >= 100 && indicator.value != PRICE && indicator.value != MACD) {
                return "value는 100 이상 입력할 수 없습니다!"
            }
        }
        // 캔들을 올바르게 입력했는지
        if (indicator.value != PRICE) {
            val validationValue = candle.value?.toIntOrNull() ?: -1
            if (validationValue <= 0) {
                return "올바른 캔들을 입력해주세요!"
            }
            if (validationValue >= 100) {
                return "캔들은 100 이상 입력할 수 없습니다!"
            }
        }
        // signal을 올바르게 입력했는지
        if (signalCondition.value != 0 && indicator.value != STOCHASTIC) {
            val validationValue = signal.value?.toIntOrNull() ?: -1
            if (validationValue <= 0) {
                return "올바른 시그널 값을 입력해주세요!"
            }
            if (validationValue >= 100) {
                return "시그널 값은 100 이상 입력할 수 없습니다!"
            }
        }
        return VALIDATION_OK
    }

    fun getCoinSearchDto(minute: Int) =
        CoinSearchDto(
            minute,
            indicator.value,
            candle.value?.toInt(),
            stochasticK.value?.toInt(),
            stochasticD.value?.toInt(),
            macdM.value?.toInt(),
            limitValue.value?.toDouble(),
            valueCondition.value,
            signal.value?.toInt(),
            signalCondition.value
        )

    companion object {
        const val VALIDATION_OK = "success"
    }
}
