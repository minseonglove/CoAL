package com.minseonglove.coal.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import com.minseonglove.coal.api.data.Constants.MACD
import com.minseonglove.coal.api.data.Constants.MOVING_AVERAGE
import com.minseonglove.coal.api.data.Constants.PRICE
import com.minseonglove.coal.api.data.Constants.RSI
import com.minseonglove.coal.api.data.Constants.STOCHASTIC
import com.minseonglove.coal.api.data.Constants.makeMarketCode
import com.minseonglove.coal.api.dto.CandleInfo
import com.minseonglove.coal.api.repository.CheckCandleRepository
import com.minseonglove.coal.ui.coin_search.CoinSearchDto
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchResultService : Service() {

    @Inject
    lateinit var checkCandleRepo: CheckCandleRepository
    private lateinit var condition: CoinSearchDto
    private lateinit var signalArray: Array<Double>
    private lateinit var mCallback: ICallBack
    private lateinit var searchJob: Job

    private val binder = SearchResultBinder()
    private val resultList = mutableListOf<String>()

    private var currentCoin: String = ""

    inner class SearchResultBinder : Binder() {
        fun getService(): SearchResultService = this@SearchResultService
    }

    interface ICallBack {
        fun updateCount(count: Int)
        fun updateList(list: List<String>)
    }

    override fun onBind(intent: Intent?): Binder {
        condition = intent!!.getParcelableExtra("condition")!!
        return binder
    }

    fun registerCallback(callback: ICallBack) {
        mCallback = callback
    }

    fun getStarted(coinList: List<String>) {
        Logger.i("bound service 시작")
        val isSignal = condition.signalCondition != 0
        signalArray = if (isSignal) Array(101) { 0.0 } else Array(1) { 0.0 }
        searchJob = CoroutineScope(Dispatchers.IO).launch {
            for (i in coinList.indices) {
                mCallback.updateCount(i + 1)
                currentCoin = coinList[i]
                val candles = CalcIndicatorUtil.getCandleCount(
                    condition.indicator,
                    condition.candle,
                    isSignal,
                    condition.stochasticK,
                    condition.stochasticD
                )
                checkCandleRepo.checkCandle(
                    condition.minute,
                    makeMarketCode(coinList[i]),
                    candles
                ).let {
                    if (it.isSuccessful) {
                        calcIndicator(it.body()!!)
                    } else {
                        Logger.e(it.errorBody().toString())
                    }
                }
                delay(SLEEP_TIME)
            }
        }
    }

    private fun calcIndicator(list: List<CandleInfo>) {
        with(condition) {
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

    private fun validatePrice(price: Double, value: Double, valueCondition: Int) {
        if (CalcIndicatorUtil.validatePrice(price, value, valueCondition)) {
            addList()
        }
    }

    private fun validateMovingAverage(
        list: List<CandleInfo>,
        tradePrice: Double,
        valueCondition: Int
    ) {
        if (CalcIndicatorUtil.validateMovingAverage(list, tradePrice, valueCondition)) {
            addList()
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
            addList()
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
            addList()
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
            addList()
        }
    }

    private fun addList() {
        resultList.add(currentCoin)
        mCallback.updateList(resultList.toList())
    }

    override fun onUnbind(intent: Intent?): Boolean {
        searchJob.cancel()
        return super.onUnbind(intent)
    }

    companion object {
        const val SLEEP_TIME = 100L
    }
}
