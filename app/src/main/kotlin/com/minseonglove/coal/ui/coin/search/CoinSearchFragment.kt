package com.minseonglove.coal.ui.coin.search

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.FragmentCoinSearchBinding
import com.minseonglove.coal.ui.alarm.setting.SettingConditionViewModel
import com.minseonglove.coal.ui.alarm.setting.SettingConditionViewModel.Companion.VALIDATION_OK
import com.minseonglove.coal.ui.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CoinSearchFragment : BaseFragment<FragmentCoinSearchBinding>(
    R.layout.fragment_coin_search
) {

    private val viewModel: SettingConditionViewModel by viewModels()

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_coinSearchFragment_to_alarmListFragment)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        initListener()
        initSpinner()
        initCollector()
    }

    private fun initCollector() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.indicator.collectLatest {
                    showSettingDisplay()
                }
            }
        }
    }

    private fun showSettingDisplay() {
        viewModel.getVisible()
    }

    private fun initListener() {
        binding.toolbarCoinsearch.buttonToolbarNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_coinSearchFragment_to_alarmListFragment)
        }
        binding.bottomButtonCoinsearch.buttonBottomAction.setOnClickListener {
            toSearchResultFragment()
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            backPressedCallback
        )
    }

    private fun initSpinner() {
        val minuteItems = resources.getStringArray(R.array.minute_items)
        val indicatorItems = resources.getStringArray(R.array.indicator_items)
        val upDownItems = resources.getStringArray(R.array.up_down_items)
        val crossItems = resources.getStringArray(R.array.cross_items)
        with(binding.settingConditionCoinsearch) {
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

    private fun toSearchResultFragment() {
        val minuteItems = resources.getStringArray(R.array.minute_items)
        val dto = viewModel.getCoinSearchDto(minuteItems[viewModel.minutePos.value].toInt())
        CoinSearchFragmentDirections.actionCoinSearchFragmentToSearchResultFragment(dto).let {
            viewModel.validateCondition().let { msg ->
                if (msg == VALIDATION_OK) {
                    findNavController().navigate(it)
                } else {
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressedCallback.remove()
    }
}
