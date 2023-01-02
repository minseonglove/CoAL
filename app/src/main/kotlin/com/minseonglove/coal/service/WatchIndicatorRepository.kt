package com.minseonglove.coal.service

import com.minseonglove.coal.api.data.Constants.MACD
import com.minseonglove.coal.api.data.Constants.MOVING_AVERAGE
import com.minseonglove.coal.api.data.Constants.PRICE
import com.minseonglove.coal.api.data.Constants.RSI
import com.minseonglove.coal.api.data.Constants.STOCHASTIC
import com.minseonglove.coal.api.data.Constants.makeMarketCode
import com.minseonglove.coal.api.dto.CandleInfo
import com.minseonglove.coal.api.repository.CheckCandleRepository
import com.minseonglove.coal.db.MyAlarm
import com.minseonglove.coal.service.CalcIndicatorUtil.getCandleCount
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class WatchIndicatorRepository(
    private val checkCandleRepo: CheckCandleRepository,
    private val alarm: MyAlarm,
    private val alarmNotify: () -> Unit,
    private val updateRunningState: (Boolean, Int) -> Unit
) {

    private val marketCode = makeMarketCode(alarm.coinName)
    private val signalArray = Array(101) { 0.0 }

    private val candleList = MutableSharedFlow<List<CandleInfo>>()
    private val watchIndicatorScope = CoroutineScope(Dispatchers.Default).launch {
        candleList.collect {
            calcIndicator(it)
        }
    }
    private lateinit var calcIndicatorScope: Job

    private var isRunning = true
    private var isFirstCandle = true
    private var currentTime = ""

    fun getCandles() {
        calcIndicatorScope = CoroutineScope(Dispatchers.IO).launch {
            while (isRunning) {
                val candles = getCandleCount(
                    alarm.indicator,
                    alarm.candle,
                    isFirstCandle,
                    alarm.stochasticK,
                    alarm.stochasticD
                )
                isFirstCandle = false
                checkCandleRepo.checkCandle(alarm.minute, marketCode, candles).let {
                    if (it.isSuccessful) {
                        val candleInfo = it.body() ?: return@let
                        validateTime(candleInfo[0].dateTime)
                        candleList.emit(candleInfo)
                    } else {
                        Logger.e(it.errorBody().toString())
                    }
                }
                delay(3000)
            }
        }
    }

    private fun calcIndicator(list: List<CandleInfo>) {
        with(alarm) {
            when (indicator) {
                PRICE -> validatePrice(list[0].tradePrice, value!!, valueCondition)
                MOVING_AVERAGE -> validateMovingAverage(list, list[0].tradePrice, valueCondition)
                RSI -> validateRSI(
                    list,
                    signalArray,
                    candle!!,
                    value!!,
                    valueCondition,
                    signal,
                    signalCondition
                )
                STOCHASTIC -> validateStochastic(
                    list,
                    candle!!,
                    stochasticK!!,
                    stochasticD!!,
                    value!!,
                    valueCondition,
                    signalCondition
                )
                MACD -> validateMACD(
                    list,
                    signalArray,
                    candle!!,
                    macdM!!,
                    value!!,
                    valueCondition,
                    signal,
                    signalCondition
                )
            }
        }
    }

    // 다음 캔들로 넘어갔는지 검사
    private fun validateTime(time: String) {
        if (currentTime != time) {
            currentTime = time
            isFirstCandle = true
        }
    }

    private fun validatePrice(price: Double, value: Double, valueCondition: Int) {
        if (CalcIndicatorUtil.validatePrice(price, value, valueCondition)) {
            validationComplete()
        }
    }

    private fun validateMovingAverage(
        list: List<CandleInfo>,
        tradePrice: Double,
        valueCondition: Int
    ) {
        if (CalcIndicatorUtil.validateMovingAverage(list, tradePrice, valueCondition)) {
            validationComplete()
        }
    }

    private fun validateRSI(
        list: List<CandleInfo>,
        signalArray: Array<Double>,
        candle: Int,
        value: Double,
        valueCondition: Int,
        signal: Int?,
        signalCondition: Int
    ) {
        if (CalcIndicatorUtil.validateRSI(
                list,
                signalArray,
                candle,
                value,
                valueCondition,
                signal,
                signalCondition
            )
        ) {
            validationComplete()
        }
    }

    private fun validateStochastic(
        list: List<CandleInfo>,
        candle: Int,
        stochasticK: Int,
        stochasticD: Int,
        value: Double,
        valueCondition: Int,
        signalCondition: Int
    ) {
        if (CalcIndicatorUtil.validateStochastic(
                list,
                candle,
                stochasticK,
                stochasticD,
                value,
                valueCondition,
                signalCondition
            )
        ) {
            validationComplete()
        }
    }

    private fun validateMACD(
        list: List<CandleInfo>,
        signalArray: Array<Double>,
        candle: Int,
        macdM: Int,
        value: Double,
        valueCondition: Int,
        signal: Int?,
        signalCondition: Int
    ) {
        if (CalcIndicatorUtil.validateMACD(
                list,
                signalArray,
                candle,
                macdM,
                value,
                valueCondition,
                signal,
                signalCondition
            )
        ) {
            validationComplete()
        }
    }

    private fun validationComplete() {
        updateRunningState(false, alarm.id)
        isRunning = false
        alarmNotify()
        cancelCollect()
    }

    fun cancelCollect() {
        watchIndicatorScope.cancel()
        calcIndicatorScope.cancel()
    }
}
