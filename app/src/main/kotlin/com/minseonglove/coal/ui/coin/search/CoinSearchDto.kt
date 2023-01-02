package com.minseonglove.coal.ui.coin.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CoinSearchDto(
    val minute: Int,
    val indicator: Int,
    val candle: Int?,
    val stochasticK: Int?,
    val stochasticD: Int?,
    val macdM: Int?,
    val value: Double?,
    val valueCondition: Int,
    val signal: Int?,
    val signalCondition: Int
) : Parcelable
