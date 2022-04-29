package com.minseonglove.coal.ui.alarm_list

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.minseonglove.coal.R
import com.minseonglove.coal.api.data.Constants.Companion.MACD
import com.minseonglove.coal.api.data.Constants.Companion.MOVING_AVERAGE
import com.minseonglove.coal.api.data.Constants.Companion.PRICE
import com.minseonglove.coal.api.data.Constants.Companion.RSI
import com.minseonglove.coal.api.data.Constants.Companion.STOCHASTIC
import com.minseonglove.coal.databinding.RecyclerAlarmListBinding
import com.minseonglove.coal.db.MyAlarm

class AlarmListAdapter(
    private val indicatorItems: Array<String>,
    private val upDownItems: Array<String>,
    private val crossItems: Array<String>,
    private val updateRunningState: (Boolean, Int) -> Unit,
    private val deleteAlarmById: (Int) -> Unit
) : ListAdapter<MyAlarm, AlarmListAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(
        val binding: RecyclerAlarmListBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyAlarm) {
            with(binding) {
                textviewAlarmlistName.apply {
                    text = makeCoinNameString(item)
                    paint.shader = textGradient(this, "#80000000", "#FF000000")
                }

                textviewAlarmlistCondition.apply {
                    text = makeConditionString(item)
                    paint.shader = textGradient(this, "#80196065", "#FF196065")
                }

                switchAlarmlistRunning.isChecked = item.isRunning

                switchAlarmlistRunning.setOnCheckedChangeListener { _, isChecked ->
                    // 핸들러로 감싸지 않으면 애니메이션이 동작 안함
                    android.os.Handler(Looper.getMainLooper()).postDelayed({
                        updateRunningState(isChecked, item.id)
                    }, 200)
                }

                switchAlarmlistRunning.setOnLongClickListener {
                    popUpDeleteButton()
                }

                buttonAlarmlistDelete.setOnClickListener {
                    deleteAlarmById(item.id)
                }

                buttonAlarmlistDelete.setOnLongClickListener {
                    popUpSwitch()
                }

                constraintlayoutAlarmlist.setOnLongClickListener {
                    if(switchAlarmlistRunning.visibility == View.VISIBLE) {
                        popUpDeleteButton()
                    } else {
                        popUpSwitch()
                    }
                }

                constraintlayoutAlarmlist.setOnClickListener {
                    if(switchAlarmlistRunning.visibility == View.GONE) {
                        popUpSwitch()
                    }
                }
            }
        }
        // 코인 이름과 분봉
        private fun makeCoinNameString(alarm: MyAlarm): String =
            "${alarm.coinName.substringBefore('(')} ${alarm.minute}분"

        // 조건 내용
        private fun makeConditionString(alarm: MyAlarm): String {
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

        // 텍스트 그라데이션
        private fun textGradient(text: TextView, startColor: String, endColor: String) =
            LinearGradient(
                0f, 0f, 0f, text.lineHeight.toFloat(),
                Color.parseColor(startColor), Color.parseColor(endColor), Shader.TileMode.REPEAT
            )

        //애니메이션 로더
        private fun getAnimation(animationId: Int) =
            AnimationUtils.loadAnimation(binding.root.context, animationId)

        //애니메이션 리스너
        private fun setAnimation(
            button1: View,
            button2: View,
            animation: Animation
        ): Animation.AnimationListener {
            return object : Animation.AnimationListener {
                override fun onAnimationStart(anim: Animation?) {}

                override fun onAnimationEnd(anim: Animation?) {
                    button1.visibility = View.GONE
                    button2.visibility = View.VISIBLE
                    button2.startAnimation(animation)
                }

                override fun onAnimationRepeat(anim: Animation?) {}
            }
        }

        // 스위치 팝업
        private fun popUpSwitch(): Boolean {
            with(binding) {
                getAnimation(R.anim.animation_delete_button_shrink).let { anim ->
                    anim.setAnimationListener(setAnimation(
                        buttonAlarmlistDelete,
                        switchAlarmlistRunning,
                        getAnimation(R.anim.animation_xscale_out)
                    ))
                    buttonAlarmlistDelete.startAnimation(anim)
                }
            }
            return true
        }

        // 삭제 버튼 팝업
        private fun popUpDeleteButton(): Boolean {
            with(binding) {
                getAnimation(R.anim.animation_xscale_in).let { anim ->
                    anim.setAnimationListener(setAnimation(
                        switchAlarmlistRunning,
                        buttonAlarmlistDelete,
                        getAnimation(R.anim.animation_delete_button_popup)
                    ))
                    switchAlarmlistRunning.startAnimation(anim)
                }
            }
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.recycler_alarm_list, parent, false)
        return ViewHolder(RecyclerAlarmListBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MyAlarm>() {
            override fun areItemsTheSame(oldItem: MyAlarm, newItem: MyAlarm) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MyAlarm, newItem: MyAlarm) =
                oldItem.id == newItem.id
        }
    }
}
