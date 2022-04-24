package com.minseonglove.coal.ui.alarm_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.FragmentAlarmListBinding
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmListFragment : Fragment() {

    private lateinit var _binding: FragmentAlarmListBinding

    private val binding get() = _binding
    private val viewModel: AlarmListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate<FragmentAlarmListBinding>(
            inflater,
            R.layout.fragment_alarm_list,
            container,
            false
        ).apply {
            vm = viewModel
            lifecycleOwner = this@AlarmListFragment
        }
        Logger.addLogAdapter(AndroidLogAdapter())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 알람 리스트 -> 조건에 맞는 코인 검색
        binding.toolbarAlarmlist.buttonToolbarNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_alarmListFragment_to_coinSearchFragment)
        }
        // 알람 리스트 -> 알람 설정
        binding.bottomButtonAlarmlist.buttonBottomAction.setOnClickListener {
            findNavController().navigate(R.id.action_alarmListFragment_to_alarmSettingFragment)
        }
    }
}