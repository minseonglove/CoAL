package com.minseonglove.coal.ui.alarm_list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.FragmentAlarmListBinding
import com.minseonglove.coal.db.MyAlarm
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmListFragment : Fragment() {

    private lateinit var alarmListAdapter: AlarmListAdapter

    private var _binding: FragmentAlarmListBinding? = null

    private val binding get() = _binding!!
    private val viewModel: AlarmListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_alarm_list,
            container,
            false
        )
        return binding.run {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNavigation()
        collectAlarmList()
    }

    private fun initNavigation() {
        // 알람 리스트 -> 조건에 맞는 코인 검색
        binding.toolbarAlarmlist.buttonToolbarNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_alarmListFragment_to_coinSearchFragment)
        }
        // 알람 리스트 -> 알람 설정
        binding.bottomButtonAlarmlist.buttonBottomAction.setOnClickListener {
            findNavController().navigate(R.id.action_alarmListFragment_to_alarmSettingFragment)
        }
    }

    private fun collectAlarmList() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getAlarmList.collect {
                    if (it.isNotEmpty()) {
                        updateRecyclerView(it)
                        it.forEach { alarm ->
                            Log.d("coin", alarm.toString())
                        }
                    }
                }
            }
        }
    }

    private fun updateRecyclerView(alarmList: List<MyAlarm>) {
        if (!::alarmListAdapter.isInitialized) {
            alarmListAdapter = AlarmListAdapter(
                resources.getStringArray(R.array.indicator_items),
                resources.getStringArray(R.array.up_down_items),
                resources.getStringArray(R.array.cross_items)
            ) { state, id ->
                viewModel.updateRunningState(state, id)
            }.apply {
                submitList(alarmList)
            }
            binding.recyclerAlarmlist.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = alarmListAdapter
            }
        } else {
            alarmListAdapter.submitList(alarmList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
