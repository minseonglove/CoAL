package com.minseonglove.coal.ui.alarm_list

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.FragmentAlarmListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmListFragment : Fragment(R.layout.fragment_alarm_list) {

    private val binding by viewBinding(FragmentAlarmListBinding::bind)
    private val viewModel: AlarmListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            // 알람 리스트 -> 조건에 맞는 코인 검색
            toolbarAlarmlist.buttonToolbarNavigation.setOnClickListener {
                findNavController().navigate(R.id.action_alarmListFragment_to_coinSearchFragment)
            }
            // 알람 리스트 -> 알람 설정
            bottomButtonAlarmlist.buttonBottomAction.setOnClickListener {
                findNavController().navigate(R.id.action_alarmListFragment_to_alarmSettingFragment)
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.testList.collect {
                    if (it.isNotEmpty()) {
                        it.forEach { alarm ->
                            Log.d("coin", alarm.toString())
                        }
                    }
                }
            }
        }
        viewModel.getTest()
    }
}
