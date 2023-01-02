package com.minseonglove.coal.service

import com.minseonglove.coal.api.data.Constants
import com.minseonglove.coal.api.dto.CandleInfo
import com.orhanobut.logger.Logger

object CalcIndicatorUtil {
    // 가격은 캔들 받는 곳에서 직접 계산
    // 이동평균선
    private fun calcMA(candleList: List<CandleInfo>): Double {
        return candleList
            .map { it.tradePrice }
            .reduce { total, next -> total + next } / candleList.size
    }

    private fun calcRSI(
        candleList: List<CandleInfo>,
        n: Int,
        startPos: Int
    ): Double {
        var ad = 0.0
        var au = 0.0
        val len = startPos + 99
        for (i in len downTo len - n + 1) {
            (candleList[i].tradePrice - candleList[i - 1].tradePrice).let {
                if (it > 0) {
                    ad += it
                } else {
                    au -= it
                }
            }
        }
        ad /= n
        au /= n
        for (i in len - n downTo 1) {
            (candleList[i].tradePrice - candleList[i - 1].tradePrice).let {
                when {
                    it > 0 -> {
                        ad = (ad * (n - 1) + it) / n
                        au = (au * (n - 1)) / n
                    }
                    it < 0 -> {
                        ad = (ad * (n - 1)) / n
                        au = (au * (n - 1) - it) / n
                    }
                    else -> {
                        ad = (ad * (n - 1)) / n
                        au = (au * (n - 1)) / n
                    }
                }
            }
        }
        return au / (au + ad) * 100
    }

    private fun calcSignal(signalArray: Array<Double>, candle: Int): Double {
        var signal = signalArray[0]
        for (i in 1..100) {
            signal = (2.0 / (candle + 1)) * signalArray[i] + (1 - 2.0 / (candle + 1)) * signal
        }
        return signal
    }

    private fun calcStochastic(
        candleList: List<CandleInfo>,
        n: Int,
        k: Int,
        d: Int
    ): Pair<Double, Double> {
        // 캔들 수는 n+(k-1)+(d-1)
        var currentPrice: Double
        var maxPrice: Double
        var minPrice: Double
        var stochastic = 0.0
        var stochasticMA = 0.0
        for (i in 0 until d) { // stoch slow %D (스토캐스틱 슬로우의 이평선)
            var currentStochastic = 0.0
            for (j in i until i + k) { // stoch slow (스토캐스틱의 이평선)
                candleList[j].apply { // 최근 캔들 값 저장
                    currentPrice = tradePrice
                    minPrice = lowPrice
                    maxPrice = highPrice
                }
                for (m in j + 1 until j + n) { // stoch
                    candleList[m].apply { // 최고가 최저가 저장
                        minPrice = if (minPrice > lowPrice) lowPrice else minPrice
                        maxPrice = if (maxPrice < highPrice) highPrice else maxPrice
                    }
                }
                // 이동평균을 내기 위한 stoch값계산
                // (당일종가 - 최근 n일동안 최저가) / 최근 n일동안 최고가 - 최근 n일동안 최저가 * 100
                currentStochastic += (currentPrice - minPrice) / (maxPrice - minPrice) * 100
            }
            // stoch slow값 계산
            stochasticMA += currentStochastic / k
            if (i == 0) {
                stochastic = stochasticMA
            }
        }
        return Pair(stochastic, stochasticMA / d)
    }

    private fun calcMACD(
        candleList: List<CandleInfo>,
        n: Int,
        m: Int,
        startPos: Int
    ): Double {
        var shortMA = candleList[startPos + 99].tradePrice
        var longMA = candleList[startPos + 99].tradePrice
        for (i in startPos + 98 downTo startPos) {
            candleList[i].apply {
                shortMA = tradePrice * (2.0 / (n + 1)) + shortMA * (1 - 2.0 / (n + 1))
                longMA = tradePrice * (2.0 / (m + 1)) + longMA * (1 - 2.0 / (m + 1))
            }
        }
        return shortMA - longMA
    }

    // 캔들 갯수
    fun getCandleCount(
        indicator: Int,
        candle: Int?,
        isFirstCandle: Boolean,
        stochasticK: Int? = 0,
        stochasticD: Int? = 0
    ): Int =
        when (indicator) {
            Constants.PRICE -> 1
            Constants.MOVING_AVERAGE -> candle!!
            Constants.STOCHASTIC -> candle!! + stochasticK!! + stochasticD!! - 2
            Constants.RSI, Constants.MACD -> { if (isFirstCandle) 200 else 100 }
            else -> 1
        }

    fun validatePrice(price: Double, value: Double, valueCondition: Int): Boolean {
        Logger.t("indicator").i("price $price")
        return validateValue(price, value, valueCondition)
    }

    fun validateMovingAverage(
        list: List<CandleInfo>,
        tradePrice: Double,
        valueCondition: Int
    ): Boolean {
        return calcMA(list).run {
            Logger.t("indicator").i("ma $this")
            validateValue(tradePrice, calcMA(list), valueCondition)
        }
    }

    fun validateRSI(
        list: List<CandleInfo>,
        signalArray: Array<Double>,
        candle: Int,
        value: Double,
        valueCondition: Int,
        signal: Int?,
        signalCondition: Int
    ): Boolean {
        // 다음 캔들의 첫 계산일 때는 캔들이 200개 들어온다.
        if (list.size == 200) {
            for (i in 1..100) {
                signalArray[i] = calcRSI(list, candle, i)
            }
        }
        // 아닐 때는 100개만 들어오고 한번만 계산
        signalArray[0] = calcRSI(list, candle, 0)
        Logger.t("indicator").i("rsi ${signalArray[0]}")
        if (validateValue(signalArray[0], value, valueCondition)) {
            if (signalCondition != 0) {
                calcSignal(signalArray, signal!!).let {
                    if (validateValue(signalArray[0], it, signalCondition, 1)) {
                        return true
                    }
                }
            } else {
                return true
            }
        }
        return false
    }

    fun validateStochastic(
        list: List<CandleInfo>,
        candle: Int,
        stochasticK: Int,
        stochasticD: Int,
        value: Double,
        valueCondition: Int,
        signalCondition: Int
    ): Boolean {
        calcStochastic(
            list,
            candle,
            stochasticK,
            stochasticD
        ).let {
            Logger.t("indicator").i("stoch ${it.first} ${it.second}")
            if (validateValue(it.first, value, valueCondition)) {
                if (signalCondition != 0) {
                    if (validateValue(it.first, it.second, signalCondition, 1)) {
                        return true
                    }
                } else {
                    return true
                }
            }
            return false
        }
    }

    fun validateMACD(
        list: List<CandleInfo>,
        signalArray: Array<Double>,
        candle: Int,
        macdM: Int,
        value: Double,
        valueCondition: Int,
        signal: Int?,
        signalCondition: Int
    ): Boolean {
        // 다음 캔들의 첫 계산일 때는 캔들이 200개 들어온다.
        if (list.size == 200) {
            for (i in 1..100) {
                signalArray[i] = calcMACD(
                    list,
                    candle,
                    macdM,
                    i
                )
            }
        }
        // 아닐 때는 100개만 들어오고 한번만 계산
        signalArray[0] = calcMACD(list, candle, macdM, 0)
        Logger.t("indicator").i("macd ${signalArray[0]}")
        if (validateValue(signalArray[0], value, valueCondition)) {
            if (signalCondition != 0) {
                calcSignal(signalArray, signal!!).let {
                    if (validateValue(signalArray[0], it, signalCondition, 1)) {
                        return true
                    }
                }
            } else {
                return true
            }
        }
        return false
    }

    private fun validateValue(
        resultValue: Double,
        settingValue: Double,
        condition: Int,
        isSignal: Int = 0
    ): Boolean =
        (resultValue > settingValue && condition == 0 + isSignal) ||
            (resultValue < settingValue && condition == 1 + isSignal)
}
