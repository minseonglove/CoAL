package com.minseonglove.coal.ui.alarm_setting

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import androidx.datastore.preferences.core.emptyPreferences
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.minseonglove.coal.R
import com.minseonglove.coal.api.data.Constants
import com.minseonglove.coal.api.data.Constants.Companion.datastore
import com.minseonglove.coal.databinding.FragmentAlarmSettingBinding
import com.minseonglove.coal.ui.setting_condition.IndicatorType
import com.minseonglove.coal.ui.setting_condition.IndicatorType.PRICE
import com.minseonglove.coal.ui.setting_condition.IndicatorType.MOVING_AVERAGE
import com.minseonglove.coal.ui.setting_condition.IndicatorType.RSI
import com.minseonglove.coal.ui.setting_condition.IndicatorType.STOCHASTIC
import com.minseonglove.coal.ui.setting_condition.IndicatorType.MACD
import com.minseonglove.coal.ui.setting_condition.SettingConditionViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmSettingFragment : Fragment(R.layout.fragment_alarm_setting) {

    private val binding by viewBinding(FragmentAlarmSettingBinding::bind)
    private val alarmSettingViewModel: AlarmSettingViewModel by viewModels()
    private val settingConditionViewModel: SettingConditionViewModel by viewModels()

    private val coinName: Flow<String> by lazy {
        requireContext().datastore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[Constants.SAVED_SELECTED_COIN]?.toString() ?: "비트코인(KRW-BTC)"
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            settingConditionViewModel = this@AlarmSettingFragment.settingConditionViewModel
            alarmSettingViewModel = this@AlarmSettingFragment.alarmSettingViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        binding.toolbarAlarmsetting.buttonToolbarNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_alarmSettingFragment_to_alarmListFragment)
        }

        binding.bottomButtonAlarmsetting.buttonBottomAction.setOnClickListener {
            addAlarm()
            findNavController().navigate(R.id.action_alarmSettingFragment_to_alarmListFragment)
        }

        initCoinName()
        initSpinner()
        initCollector()
    }

    private fun initCoinName() {
        lifecycleScope.launch {
            coinName.collect {
                alarmSettingViewModel.setCoinName(it)
            }
        }
    }

    private fun initCollector() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingConditionViewModel.indicator.collectLatest { indicator ->
                    showSettingDisplay(IndicatorType.fromInt(indicator))
                }
            }
        }
    }

    private fun addAlarm() {
        val minuteItems = resources.getStringArray(R.array.minute_items)
        alarmSettingViewModel.addAlarm(
            settingConditionViewModel.getAlarm(
                alarmSettingViewModel.selectedCoin.value,
                minuteItems[settingConditionViewModel.minutePos.value].toInt()
            )
        )
    }

    private fun showSettingDisplay(indicator: IndicatorType) {
        displayGoneAll()
        with(binding.settingConditionAlarmsetting) {
            when (indicator) {
                PRICE -> {
                    textviewSettingPrice.visibility = View.VISIBLE
                    edittextSettingPrice.visibility = View.VISIBLE
                    spinnerSettingPrice.visibility = View.VISIBLE
                }
                MOVING_AVERAGE -> {
                    textviewSettingCandles.visibility = View.VISIBLE
                    edittextSettingCandles.visibility = View.VISIBLE
                    spinnerSettingMa.visibility = View.VISIBLE
                }
                RSI -> {
                    textviewSettingCandles.visibility = View.VISIBLE
                    textviewSettingValue.visibility = View.VISIBLE
                    textviewSettingSignal.visibility = View.VISIBLE
                    edittextSettingCandles.visibility = View.VISIBLE
                    edittextSettingValue.visibility = View.VISIBLE
                    edittextSettingValue.hint = getString(R.string.setting_condition_hint_percent)
                    edittextSettingSignal.visibility = View.VISIBLE
                    spinnerSettingValue.visibility = View.VISIBLE
                    spinnerSettingSignal.visibility = View.VISIBLE
                }
                STOCHASTIC -> {
                    textviewSettingStochastic.visibility = View.VISIBLE
                    textviewSettingStochasticCross.visibility = View.VISIBLE
                    textviewSettingValue.visibility = View.VISIBLE
                    edittextSettingN.visibility = View.VISIBLE
                    edittextSettingK.visibility = View.VISIBLE
                    edittextSettingD.visibility = View.VISIBLE
                    edittextSettingValue.visibility = View.VISIBLE
                    edittextSettingValue.hint = getString(R.string.setting_condition_hint_percent)
                    spinnerSettingValue.visibility = View.VISIBLE
                    spinnerSettingStochasticCross.visibility = View.VISIBLE
                }
                MACD -> {
                    textviewSettingMacd.visibility = View.VISIBLE
                    textviewSettingValue.visibility = View.VISIBLE
                    textviewSettingSignal.visibility = View.VISIBLE
                    edittextSettingMacdN.visibility = View.VISIBLE
                    edittextSettingMacdM.visibility = View.VISIBLE
                    edittextSettingValue.visibility = View.VISIBLE
                    edittextSettingValue.hint = getString(R.string.setting_condition_hint_value)
                    edittextSettingSignal.visibility = View.VISIBLE
                    spinnerSettingValue.visibility = View.VISIBLE
                    spinnerSettingSignal.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun displayGoneAll() {
        with(binding.settingConditionAlarmsetting) {
            textviewSettingPrice.visibility = View.GONE
            textviewSettingCandles.visibility = View.GONE
            textviewSettingStochastic.visibility = View.GONE
            textviewSettingStochasticCross.visibility = View.GONE
            textviewSettingMacd.visibility = View.GONE
            textviewSettingValue.visibility = View.GONE
            textviewSettingSignal.visibility = View.GONE
            edittextSettingPrice.visibility = View.GONE
            edittextSettingCandles.visibility = View.GONE
            edittextSettingD.visibility = View.GONE
            edittextSettingK.visibility = View.GONE
            edittextSettingMacdM.visibility = View.GONE
            edittextSettingMacdN.visibility = View.GONE
            edittextSettingN.visibility = View.GONE
            edittextSettingSignal.visibility = View.GONE
            edittextSettingValue.visibility = View.GONE
            spinnerSettingStochasticCross.visibility = View.GONE
            spinnerSettingValue.visibility = View.GONE
            spinnerSettingSignal.visibility = View.GONE
            spinnerSettingPrice.visibility = View.GONE
            spinnerSettingMa.visibility = View.GONE
        }
    }

    private fun initSpinner() {
        val minuteItems = resources.getStringArray(R.array.minute_items)
        val indicatorItems = resources.getStringArray(R.array.indicator_items)
        val upDownItems = resources.getStringArray(R.array.up_down_items)
        val crossItems = resources.getStringArray(R.array.cross_items)
        with(binding.settingConditionAlarmsetting) {
            buttonSettingSelectCoin.setOnClickListener {
                findNavController().navigate(R.id.action_alarmSettingFragment_to_coinSelectFragment)
            }
            spinnerSettingMinutes.adapter = createAdapter(minuteItems)
            spinnerSettingIndicator.adapter = createAdapter(indicatorItems)
            spinnerSettingPrice.adapter = createAdapter(upDownItems)
            spinnerSettingMa.adapter = createAdapter(upDownItems)
            spinnerSettingValue.adapter = createAdapter(upDownItems)
            spinnerSettingSignal.adapter = createAdapter(crossItems)
            spinnerSettingStochasticCross.adapter = createAdapter(crossItems)
        }
    }

    private fun createAdapter(items: Array<String>): SpinnerAdapter =
        ArrayAdapter(requireContext(), R.layout.spinner_item, items)
}
