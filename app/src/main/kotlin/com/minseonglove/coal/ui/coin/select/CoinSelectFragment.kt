package com.minseonglove.coal.ui.coin.select

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout.VERTICAL
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.minseonglove.coal.R
import com.minseonglove.coal.api.data.Constants
import com.minseonglove.coal.api.data.Constants.datastore
import com.minseonglove.coal.databinding.FragmentCoinSelectBinding
import com.minseonglove.coal.ui.base.BaseFragment
import com.minseonglove.coal.ui.util.repeatOnStarted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

class CoinSelectFragment : BaseFragment<FragmentCoinSelectBinding>(
    R.layout.fragment_coin_select
) {

    private val coinSelectAdapter: CoinListAdapter by lazy {
        CoinListAdapter { coinName -> saveSelectedCoin(coinName) }
    }

    private val viewModel: CoinSelectViewModel by viewModels()

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_coinSelectFragment_to_alarmSettingFragment)
            }
        }
    }

    private val coinList: Flow<List<String>> by lazy {
        requireContext().datastore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[Constants.SAVED_COIN_LIST]?.toList() ?: listOf()
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        binding.toolbarCoinselect.buttonToolbarNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_coinSelectFragment_to_alarmSettingFragment)
        }

        initCollector()
    }

    private fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            coinList.collect {
                it.sorted().let { sortedList ->
                    viewModel.setOriginalList(sortedList)
                    initRecyclerView(sortedList)
                }
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.searchedList.collectLatest {
                coinSelectAdapter.submitList(it)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            backPressedCallback
        )
    }

    private fun initRecyclerView(list: List<String>) {
        val divider = DividerItemDecoration(requireContext(), VERTICAL).apply {
            setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider_coin_list)!!)
        }
        binding.recyclerCoinselect.apply {
            addItemDecoration(divider)
            adapter = coinSelectAdapter
        }
        coinSelectAdapter.submitList(list)
    }

    private fun saveSelectedCoin(coinName: String) {
        lifecycleScope.launch {
            requireContext().datastore.edit { pref ->
                pref[Constants.SAVED_SELECTED_COIN] = coinName
            }
            findNavController().navigate(R.id.action_coinSelectFragment_to_alarmSettingFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressedCallback.remove()
    }
}
