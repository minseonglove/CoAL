package com.minseonglove.coal.ui.alarm_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.RecyclerAlarmListBinding
import com.minseonglove.coal.db.MyAlarm
import com.minseonglove.coal.ui.setting_condition.IndicatorType
import com.minseonglove.coal.ui.setting_condition.IndicatorType.PRICE
import com.minseonglove.coal.ui.setting_condition.IndicatorType.MOVING_AVERAGE
import com.minseonglove.coal.ui.setting_condition.IndicatorType.RSI
import com.minseonglove.coal.ui.setting_condition.IndicatorType.STOCHASTIC
import com.minseonglove.coal.ui.setting_condition.IndicatorType.MACD

class AlarmListAdapter(
    private var alarmList: List<MyAlarm>,
    private val indicatorItems: Array<String>,
    private val upDownItems: Array<String>,
    private val crossItems: Array<String>
) : RecyclerView.Adapter<AlarmListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RecyclerAlarmListBinding) :
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.recycler_alarm_list, parent, false)
        return ViewHolder(RecyclerAlarmListBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            textviewAlarmlistName.text = makeCoinNameString(alarmList[position])
            textviewAlarmlistCondition.text = makeConditionString(alarmList[position])
            switchAlarmlistRunning.isChecked = alarmList[position].isRunning
        }
    }

    override fun getItemCount() = alarmList.size

    private fun makeCoinNameString(alarm: MyAlarm): String =
        "${alarm.coinName.substringBefore('(')} ${alarm.candle}분"

    private fun makeConditionString(alarm: MyAlarm): String {
        with (alarm) {
            return StringBuilder(indicatorItems[indicator]).apply {
                appendLine(
                    when (IndicatorType.fromInt(indicator)) {
                        PRICE -> {
                            "(${value})"
                        }
                        MOVING_AVERAGE -> {
                            "(${candle}) ${upDownItems[valueCondition]}돌파"
                        }
                        RSI -> {
                            "(${candle}) ${value}% ${upDownItems[valueCondition]}돌파"
                        }
                        STOCHASTIC -> {
                            "(${candle},${stochasticK},${stochasticD}) " +
                                    "${value}% ${upDownItems[valueCondition]}돌파"
                        }
                        MACD -> {
                            "(${candle},${macdM}) $value ${upDownItems[valueCondition]}돌파"
                        }
                    }
                )
                // 시그널 사용
                if (signalCondition != 0) {
                    append("${signal}일 이동평균선 ${crossItems[signalCondition]}")
                }
            }.toString()
        }
    }
}