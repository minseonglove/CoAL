package com.minseonglove.coal.api.dto

import com.google.gson.annotations.SerializedName

data class CandleInfo(
    @SerializedName("market")
    val market: String,
    @SerializedName("opening_price")
    val openingPrice: Double,
    @SerializedName("high_price")
    val highPrice: Double,
    @SerializedName("low_price")
    val lowPrice: Double,
)
