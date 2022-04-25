package com.minseonglove.coal.ui.alarm_setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.FragmentAlarmSettingBinding

class AlarmSettingFragment : Fragment() {

    private lateinit var _binding: FragmentAlarmSettingBinding

    private val binding get() = _binding
    private val viewModel: AlarmSettingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = DataBindingUtil.inflate<FragmentAlarmSettingBinding?>(
            inflater,
            R.layout.fragment_alarm_setting,
            container,
            false
        ).apply {
            vm = viewModel
            lifecycleOwner = this@AlarmSettingFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingConditionAlarmsetting.buttonSettingSelectCoin.setOnClickListener {
            findNavController().navigate(R.id.action_alarmSettingFragment_to_coinSelectFragment)
        }
    }
}