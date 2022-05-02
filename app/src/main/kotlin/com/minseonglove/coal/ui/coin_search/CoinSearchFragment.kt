package com.minseonglove.coal.ui.coin_search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.FragmentCoinSearchBinding
import com.minseonglove.coal.ui.setting_condition.SettingConditionViewModel
import com.minseonglove.coal.ui.setting_condition.SettingConditionViewModel.Companion.VALIDATION_OK
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CoinSearchFragment : Fragment() {

    private var _binding: FragmentCoinSearchBinding? = null

    private val binding get() = _binding!!
    private val viewModel: SettingConditionViewModel by viewModels()

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_coinSearchFragment_to_alarmListFragment)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_coin_search,
            container,
            false
        )
        return binding.run {
            conditionViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        _binding = null
    }
}
