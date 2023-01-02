package com.minseonglove.coal.api.dto

import com.google.gson.annotations.SerializedName

data class CandleInfo(
    @SerializedName("market")
    val market: String,
    @SerializedName("candle_date_time_kst")
    val dateTime: String,
    @SerializedName("trade_price")
    val tradePrice: Double,
    @SerializedName("high_price")
    val highPrice: Double,
    @SerializedName("low_price")
    val lowPrice: Double
)
