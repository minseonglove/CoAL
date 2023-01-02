package com.minseonglove.coal.api.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.minseonglove.coal.db.MyAlarm

object Constants {
    const val BASE_URL = "https://api.upbit.com/v1/"
    val Context.datastore: DataStore<Preferences> by preferencesDataStore(
        name = "coin_list"
    )
    val SAVED_COIN_LIST = stringSetPreferencesKey("coin_list")
    val SAVED_SELECTED_COIN = stringPreferencesKey("coin_name")
    const val PRICE = 0
    const val MOVING_AVERAGE = 1
    const val RSI = 2
    const val STOCHASTIC = 3
    const val MACD = 4

    fun makeMarketCode(name: String) =
        name.substringAfter('(').substringBefore(')')

    fun makeCoinNameString(name: String, minute: Int) =
        "${name.substringBefore('(')} ${minute}분"

    fun makeConditionString(
        alarm: MyAlarm,
        indicatorItems: Array<String>,
        upDownItems: Array<String>,
        crossItems: Array<String>
    ): String {
        with(alarm) {
            return StringBuilder(indicatorItems[indicator]).apply {
                appendLine(
                    when (indicator) {
                        PRICE -> {
                            " $value ${coinName.substringAfter('(').substringBefore('-')} " +
                                "${upDownItems[valueCondition]}돌파"
                        }
                        MOVING_AVERAGE -> {
                            " ($candle) ${upDownItems[valueCondition]}돌파"
                        }
                        RSI -> {
                            " ($candle) $value% ${upDownItems[valueCondition]}돌파"
                        }
                        STOCHASTIC -> {
                            " ($candle,$stochasticK,$stochasticD) " +
                                "$value% ${upDownItems[valueCondition]}돌파"
                        }
                        MACD -> {
                            " ($candle,$macdM) $value ${upDownItems[valueCondition]}돌파"
                        }
                        else -> {}
                    }
                )
                // 시그널 사용
                if (signalCondition != 0) {
                    when (indicator) {
                        STOCHASTIC -> append("%K %D ${crossItems[signalCondition]}")
                        else -> append("${signal}일 이동평균선 ${crossItems[signalCondition]}")
                    }
                }
            }.toString()
        }
    }
}
