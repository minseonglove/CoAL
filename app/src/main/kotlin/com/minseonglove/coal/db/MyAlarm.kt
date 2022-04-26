package com.minseonglove.coal.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MyAlarm(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "coin_name") val coinName: String,
    @ColumnInfo(name = "minute") val minute: Int,
    @ColumnInfo(name = "indicator") val indicator: Int,
    @ColumnInfo(name = "candle") val candle: Int?,
    @ColumnInfo(name = "stochastic_k") val stochasticK: Int?,
    @ColumnInfo(name = "stochastic_d") val stochasticD: Int?,
    @ColumnInfo(name = "macd_m") val macdM: Int?,
    @ColumnInfo(name = "value") val value: Double?,
    @ColumnInfo(name = "value_condition") val valueCondition: Int,
    @ColumnInfo(name = "signal") val signal: Int?,
    @ColumnInfo(name = "signal_condition") val signalCondition: Int
)
