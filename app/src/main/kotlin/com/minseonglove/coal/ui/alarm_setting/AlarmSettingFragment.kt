package com.minseonglove.coal.ui.alarm_setting

import android.os.Bundle
import android.view.View
import androidx.datastore.preferences.core.emptyPreferences
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.minseonglove.coal.R
import com.minseonglove.coal.api.data.Constants
import com.minseonglove.coal.api.data.Constants.Companion.datastore
import com.minseonglove.coal.databinding.FragmentAlarmSettingBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

class AlarmSettingFragment : Fragment(R.layout.fragment_alarm_setting) {

    private val binding by viewBinding(FragmentAlarmSettingBinding::bind)
    private val viewModel: AlarmSettingViewModel by viewModels()

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
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        lifecycleScope.launch {
            coinName.collect {
                viewModel.setCoinName(it)
            }
        }

        binding.settingConditionAlarmsetting

        binding.settingConditionAlarmsetting.buttonSettingSelectCoin.setOnClickListener {
            findNavController().navigate(R.id.action_alarmSettingFragment_to_coinSelectFragment)
        }
    }
}
