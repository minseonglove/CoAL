package com.minseonglove.coal.ui.alarm.list

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.FragmentAlarmListBinding
import com.minseonglove.coal.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmListFragment : BaseFragment<FragmentAlarmListBinding>(
    R.layout.fragment_alarm_list
) {

    private val alarmListAdapter: AlarmListAdapter by lazy {
        AlarmListAdapter(
            resources.getStringArray(R.array.indicator_items),
            resources.getStringArray(R.array.up_down_items),
            resources.getStringArray(R.array.cross_items),
            updateRunningState = { state, id ->
                viewModel.updateRunningState(state, id)
            },
            deleteAlarmById = { id ->
                viewModel.deleteById(id)
            }
        )
    }

    private var backPressedTime = 0L

    private val viewModel: AlarmListViewModel by viewModels()

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                exitApp()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        binding.recyclerAlarmlist.adapter = alarmListAdapter
        initNavigation()
        collectAlarmList()
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
    }

    private fun exitApp() {
        System.currentTimeMillis().let { currentTime ->
            if (currentTime > backPressedTime + FINISH_TIME_OUT) {
                AnimationUtils.loadAnimation(requireContext(), R.anim.animation_shake).let {
                    binding.constraintlayoutAlarmlist.startAnimation(it)
                }
                backPressedTime = currentTime
                Toast.makeText(
                    requireContext(),
                    "한 번 더 누르면 종료 됩니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                requireActivity().finishAffinity()
            }
        }
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
                viewModel.getAlarmList.collectLatest {
                    alarmListAdapter.submitList(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressedCallback.remove()
    }

    companion object {
        private const val FINISH_TIME_OUT = 2500
    }
}
