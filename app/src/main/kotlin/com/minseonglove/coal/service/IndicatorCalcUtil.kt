package com.minseonglove.coal.service

import com.minseonglove.coal.api.dto.CandleInfo

class IndicatorCalcUtil {
    companion object {
        // 가격은 캔들 받는 곳에서 직접 계산
        // 이동평균선
        suspend fun calcMA(candleList: List<CandleInfo>): Double {
            return candleList
                .map { it.tradePrice }
                .reduce { total, next -> total + next } / candleList.size
        }
        // RSI
        suspend fun calcRSI(
            candleList: List<CandleInfo>,
            n: Int,
            startPos: Int,
        ): Double {
            var ad = 0.0
            var au = 0.0
            val len = startPos + 99
            for(i in len downTo len - n + 1) {
                (candleList[i].tradePrice - candleList[i-1].tradePrice).let {
                    if (it > 0){
                        ad += it
                    } else {
                        au -= it
                    }
                }
            }
            ad /= n
            au /= n
            for(i in len - n downTo 1) {
                (candleList[i].tradePrice - candleList[i-1].tradePrice).let {
                    if (it > 0) {
                        ad = (ad * (n - 1) + it) / n
                        au = (au * (n - 1)) / n
                    } else if (it < 0) {
                        ad = (ad * (n - 1)) / n
                        au = (au * (n - 1) - it) / n
                    } else {
                        ad = (ad * (n - 1)) / n
                        au = (au * (n - 1)) / n
                    }
                }
            }
            return au / (au + ad) * 100
        }
        // Stochastic
        // (스토치, 스토치 이평선)
        suspend fun calcStochastic(
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
                for (j in i until i+k) { // stoch slow (스토캐스틱의 이평선)
                    candleList[j].apply { // 최근 캔들 값 저장
                        currentPrice = tradePrice
                        minPrice = lowPrice
                        maxPrice = highPrice
                    }
                    for (m in j until j+n) { // stoch
                        candleList[m].apply { // 최고가 최저가 저장
                            minPrice = if(minPrice > lowPrice) lowPrice else minPrice
                            maxPrice = if(maxPrice > highPrice) highPrice else maxPrice
                        }
                    }
                    //이동평균을 내기 위한 stoch값계산
                    // (당일종가 - 최근 n일동안 최저가) / 최근 n일동안 최고가 - 최근 n일동안 최저가 * 100
                    currentStochastic += (currentPrice - minPrice) / (maxPrice - minPrice) * 100
                }
                // stoch slow값 계산
                stochasticMA += currentStochastic / k
                if (i == 0) {
                    stochastic = currentStochastic
                }
            }
            return Pair(stochastic, stochasticMA / d)
        }
        // MACD
        suspend fun calcMACD(
            candleList: List<CandleInfo>,
            n: Int,
            m: Int,
            startPos: Int
        ): Double {
            var shortMA = candleList[startPos + 99].tradePrice
            var longMA = candleList[startPos + 99].tradePrice
            for(i in startPos + 98 downTo startPos) {
                candleList[i].apply {
                    shortMA = tradePrice * (2.0 / (n + 1)) + shortMA * (1 - 2.0 / (n + 1))
                    longMA = tradePrice * (2.0 / (m + 1)) + longMA * (1 - 2.0 / (m + 1))
                }
            }
            return shortMA - longMA
        }
    }
}