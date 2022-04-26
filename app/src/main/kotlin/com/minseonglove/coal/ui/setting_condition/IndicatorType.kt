package com.minseonglove.coal.ui.setting_condition

enum class IndicatorType(val type: Int) {
    PRICE(0), MOVING_AVERAGE(1), RSI(2), STOCHASTIC(3), MACD(4);
    companion object {
        fun fromInt(type: Int) = values().first { it.type == type }
    }
}
