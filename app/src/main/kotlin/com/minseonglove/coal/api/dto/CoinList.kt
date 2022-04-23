package com.minseonglove.coal.api.dto

import com.google.gson.annotations.SerializedName

data class CoinList(
    @SerializedName("market")
    val market: String,
    @SerializedName("korean_name")
    val koreanName: String
)
