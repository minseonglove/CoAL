package com.minseonglove.coal.ui.alarm.setting

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.datastore.preferences.core.emptyPreferences
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.minseonglove.coal.R
import com.minseonglove.coal.api.data.Constants
import com.minseonglove.coal.api.data.Constants.datastore
import com.minseonglove.coal.databinding.FragmentAlarmSettingBinding
import com.minseonglove.coal.ui.alarm.setting.SettingConditionViewModel.Companion.VALIDATION_OK
import com.minseonglove.coal.ui.base.BaseFragment
import com.minseonglove.coal.ui.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class AlarmSettingFragment : BaseFragment<FragmentAlarmSettingBinding>(
    R.layout.fragment_alarm_setting
) {

    private val alarmSettingViewModel: AlarmSettingViewModel by viewModels()
    private val conditionViewModel: SettingConditionViewModel by viewModels()

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_alarmSettingFragment_to_alarmListFragment)
            }
        }
    }

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
        binding.conditionViewModel = conditionViewModel
        binding.alarmSettingViewModel = alarmSettingViewModel
        initListener()
        initCoinName()
        initSpinner()
        initCollector()
    }

    private fun initListener() {
        binding.toolbarAlarmsetting.buttonToolbarNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_alarmSettingFragment_to_alarmListFragment)
        }

        binding.bottomButtonAlarmsetting.buttonBottomAction.setOnClickListener {
            validateCondition()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            backPressedCallback
        )
    }

    private fun initCoinName() {
        lifecycleScope.launch {
            coinName.collect {
                alarmSettingViewModel.setCoinName(it)
            }
        }
    }

    private fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            conditionViewModel.indicator.collectLatest {
                showSettingDisplay()
            }
        }
    }

    private fun addAlarm() {
        val minuteItems = resources.getStringArray(R.array.minute_items)
        alarmSettingViewModel.addAlarm(
            conditionViewModel.getAlarm(
                alarmSettingViewModel.selectedCoin.value,
                minuteItems[conditionViewModel.minutePos.value].toInt()
            )
        )
    }

    private fun showSettingDisplay() {
        conditionViewModel.getVisible()
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

    private fun validateCondition() {
        conditionViewModel.validateCondition().let {
            if (it == VALIDATION_OK) {
                addAlarm()
                findNavController().navigate(R.id.action_alarmSettingFragment_to_alarmListFragment)
            } else {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressedCallback.remove()
    }
}
