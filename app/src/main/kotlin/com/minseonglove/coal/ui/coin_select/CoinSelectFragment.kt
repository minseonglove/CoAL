package com.minseonglove.coal.ui.coin_select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.minseonglove.coal.R
import com.minseonglove.coal.api.data.Constants
import com.minseonglove.coal.api.data.Constants.Companion.datastore
import com.minseonglove.coal.databinding.FragmentCoinSelectBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

class CoinSelectFragment : Fragment(R.layout.fragment_coin_select) {

    private lateinit var coinSelectAdapter: CoinSelectAdapter

    private var _binding: FragmentCoinSelectBinding? = null

    private val binding get() = _binding!!
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_coin_select,
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

        binding.toolbarCoinselect.buttonToolbarNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_coinSelectFragment_to_alarmSettingFragment)
        }

        initCollector()
    }

    private fun initCollector() {
        // 코인 목록 가져오기
        lifecycleScope.launch {
            coinList.collect {
                it.sorted().let { sortedList ->
                    viewModel.setOriginalList(sortedList)
                    initRecyclerView(sortedList)
                }
            }
        }
        // 코인 검색
        lifecycleScope.launch {
            viewModel.searchedList.collectLatest {
                coinSelectAdapter.submitList(it)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            backPressedCallback
        )
    }

    private fun initRecyclerView(list: List<String>) {
        coinSelectAdapter = CoinSelectAdapter { coinName ->
            saveSelectedCoin(coinName)
        }.apply {
            submitList(list)
        }
        val divider = DividerItemDecoration(requireContext(), VERTICAL).apply {
            setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider_coin_list)!!)
        }
        binding.recyclerCoinselect.apply {
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(divider)
            adapter = coinSelectAdapter
        }
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
        _binding = null
    }
}
