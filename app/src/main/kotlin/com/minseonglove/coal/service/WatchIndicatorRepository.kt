package com.minseonglove.coal.service

import android.util.Log
import com.minseonglove.coal.api.data.Constants.MACD
import com.minseonglove.coal.api.data.Constants.MOVING_AVERAGE
import com.minseonglove.coal.api.data.Constants.PRICE
import com.minseonglove.coal.api.data.Constants.RSI
import com.minseonglove.coal.api.data.Constants.STOCHASTIC
import com.minseonglove.coal.api.data.Constants.makeMarketCode
import com.minseonglove.coal.api.dto.CandleInfo
import com.minseonglove.coal.api.repository.CheckCandleRepository
import com.minseonglove.coal.db.MyAlarm
import com.orhanobut.logger.AndroidLogAdapter
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
    private val updateRunningState: (Boolean, Int) -> Unit,
) {

    private val marketCode = makeMarketCode(alarm.coinName)
    private val rsiSignalArray = Array(101) { 0.0 }
    private val macdSignalArray = Array(101) { 0.0 }

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
        Logger.addLogAdapter(AndroidLogAdapter())
        calcIndicatorScope = CoroutineScope(Dispatchers.IO).launch {
            while (isRunning) {
                checkCandleRepo.checkCandle(alarm.minute, marketCode, getCandleCount()).let {
                    if (it.isSuccessful) {
                        validateTime(it.body()!![0].dateTime)
                        candleList.emit(it.body()!!)
                    } else {
                        Logger.e(it.errorBody().toString())
                    }
                }
                delay(3000)
            }
        }
    }

    private fun calcIndicator(list: List<CandleInfo>) {
        when (alarm.indicator) {
            PRICE -> validatePrice(list[0].tradePrice)
            MOVING_AVERAGE -> validateMovingAverage(list)
            RSI -> validateRSI(list)
            STOCHASTIC -> validateStochastic(list)
            MACD -> validateMACD(list)
        }
    }

    private fun getCandleCount(): Int =
        when (alarm.indicator) {
            PRICE -> 1
            MOVING_AVERAGE -> alarm.candle!!
            STOCHASTIC -> alarm.candle!! + alarm.stochasticK!! + alarm.stochasticD!! - 2
            RSI, MACD -> {
                if (isFirstCandle) {
                    isFirstCandle = false
                    200
                } else {
                    100
                }
            }
            else -> 1
        }

    // 다음 캔들로 넘어갔는지 검사
    private fun validateTime(time: String) {
        if (currentTime != time) {
            currentTime = time
            isFirstCandle = true
        }
    }

    private fun validatePrice(price: Double) {
        Log.d("coin", "price $price")
        if (validateValue(price, alarm.value!!, alarm.valueCondition)) {
            validationComplete()
        }
    }

    private fun validateMovingAverage(list: List<CandleInfo>) {
        Log.d("coin", "ma ${CalcIndicatorUtil.calcMA(list)}")
        Log.d("coin", "price ${list[0].tradePrice}")
        if (validateValue(
                list[0].tradePrice,
                CalcIndicatorUtil.calcMA(list),
                alarm.valueCondition
            )
        ) {
            validationComplete()
        }
    }

    private fun validateRSI(list: List<CandleInfo>) {
        // 다음 캔들의 첫 계산일 때는 캔들이 200개 들어온다.
        if (list.size == 200) {
            for (i in 1..100) {
                rsiSignalArray[i] = CalcIndicatorUtil.calcRSI(list, alarm.candle!!, i)
            }
        }
        // 아닐 때는 100개만 들어오고 한번만 계산
        with(alarm) {
            rsiSignalArray[0] = CalcIndicatorUtil.calcRSI(list, candle!!, 0)
            Log.d("coin", "rsi ${rsiSignalArray[0]}")
            if (validateValue(rsiSignalArray[0], value!!, valueCondition)) {
                if (signalCondition != 0) {
                    CalcIndicatorUtil.calcSignal(rsiSignalArray, signal!!).let {
                        if (validateValue(rsiSignalArray[0], it, signalCondition, 1)) {
                            validationComplete()
                        }
                    }
                } else {
                    validationComplete()
                }
            }
        }
    }

    private fun validateStochastic(list: List<CandleInfo>) {
        CalcIndicatorUtil.calcStochastic(
            list,
            alarm.candle!!,
            alarm.stochasticK!!,
            alarm.stochasticD!!
        ).let {
            Log.d("coin", "stoch ${it.first} ${it.second}")
            with(alarm) {
                if (validateValue(it.first, value!!, valueCondition)) {
                    if (signalCondition != 0) {
                        if (validateValue(it.first, it.second, signalCondition, 1)) {
                            validationComplete()
                        }
                    } else {
                        validationComplete()
                    }
                }
            }
        }
    }

    private fun validateMACD(list: List<CandleInfo>) {
        // 다음 캔들의 첫 계산일 때는 캔들이 200개 들어온다.
        if (list.size == 200) {
            for (i in 1..100) {
                macdSignalArray[i] = CalcIndicatorUtil.calcMACD(
                    list,
                    alarm.candle!!,
                    alarm.macdM!!,
                    i
                )
            }
        }
        // 아닐 때는 100개만 들어오고 한번만 계산
        with(alarm) {
            macdSignalArray[0] = CalcIndicatorUtil.calcMACD(list, candle!!, macdM!!, 0)
            Log.d("coin", "macd ${macdSignalArray[0]}")
            if (validateValue(macdSignalArray[0], value!!, valueCondition)) {
                if (signalCondition != 0) {
                    CalcIndicatorUtil.calcSignal(macdSignalArray, signal!!).let {
                        if (validateValue(macdSignalArray[0], it, signalCondition, 1)) {
                            validationComplete()
                        }
                    }
                } else {
                    validationComplete()
                }
            }
        }
    }

    private fun validateValue(
        resultValue: Double,
        settingValue: Double,
        condition: Int,
        isSignal: Int = 0
    ): Boolean =
        (resultValue > settingValue && condition == 0 + isSignal) ||
            (resultValue < settingValue && condition == 1 + isSignal)

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
