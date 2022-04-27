package com.minseonglove.coal.ui.alarm_list

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.RecyclerAlarmListBinding
import com.minseonglove.coal.db.MyAlarm
import com.minseonglove.coal.ui.setting_condition.IndicatorType
import com.minseonglove.coal.ui.setting_condition.IndicatorType.MACD
import com.minseonglove.coal.ui.setting_condition.IndicatorType.MOVING_AVERAGE
import com.minseonglove.coal.ui.setting_condition.IndicatorType.PRICE
import com.minseonglove.coal.ui.setting_condition.IndicatorType.RSI
import com.minseonglove.coal.ui.setting_condition.IndicatorType.STOCHASTIC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.logging.Handler

class AlarmListAdapter(
    private var alarmList: List<MyAlarm>,
    private val indicatorItems: Array<String>,
    private val upDownItems: Array<String>,
    private val crossItems: Array<String>,
    private val updateRunningState: (Boolean, Int) -> Unit
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
            textviewAlarmlistName.apply {
                text = makeCoinNameString(alarmList[position])
                paint.shader = textGradient(this, "#80000000", "#FF000000")
            }
            textviewAlarmlistCondition.apply {
                text = makeConditionString(alarmList[position])
                paint.shader = textGradient(this, "#80196065", "#FF196065")
            }
            switchAlarmlistRunning.isChecked = alarmList[position].isRunning
            switchAlarmlistRunning.setOnCheckedChangeListener { _, isChecked ->
                // 핸들러로 감싸지 않으면 애니메이션이 동작 안함
                android.os.Handler(Looper.getMainLooper()).postDelayed({
                    updateRunningState(isChecked, alarmList[position].id)
                }, 200)
            }
        }
    }

    override fun getItemCount() = alarmList.size

    // 코인 이름과 분봉
    private fun makeCoinNameString(alarm: MyAlarm): String =
        "${alarm.coinName.substringBefore('(')} ${alarm.minute}분"

    // 조건 내용
    private fun makeConditionString(alarm: MyAlarm): String {
        with(alarm) {
            return StringBuilder(indicatorItems[indicator]).apply {
                appendLine(
                    when (IndicatorType.fromInt(indicator)) {
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
                    }
                )
                // 시그널 사용
                if (signalCondition != 0) {
                    append("${signal}일 이동평균선 ${crossItems[signalCondition]}")
                }
            }.toString()
        }
    }

    // 텍스트 그라데이션
    private fun textGradient(text: TextView, startColor: String, endColor: String) =
        LinearGradient(
            0f, 0f, 0f, text.lineHeight.toFloat(),
            Color.parseColor(startColor), Color.parseColor(endColor), Shader.TileMode.REPEAT
        )

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(items: List<MyAlarm>) {
        alarmList = items
        notifyDataSetChanged()
    }
}
