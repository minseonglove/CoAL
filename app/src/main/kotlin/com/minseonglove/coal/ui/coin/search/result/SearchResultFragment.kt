package com.minseonglove.coal.ui.coin.search.result

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.emptyPreferences
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.minseonglove.coal.R
import com.minseonglove.coal.api.data.Constants
import com.minseonglove.coal.api.data.Constants.datastore
import com.minseonglove.coal.api.data.Constants.makeConditionString
import com.minseonglove.coal.databinding.FragmentSearchResultBinding
import com.minseonglove.coal.db.MyAlarm
import com.minseonglove.coal.service.SearchResultService
import com.minseonglove.coal.ui.coin.select.CoinListAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

class SearchResultFragment : Fragment() {

    private lateinit var searchResultAdapter: CoinListAdapter
    private lateinit var searchResultService: SearchResultService
    private var backPressedTime = 0L
    private var _binding: FragmentSearchResultBinding? = null
    private var isBound = false

    private val binding get() = _binding!!
    private val args: SearchResultFragmentArgs by navArgs()
    private val viewModel: SearchResultViewModel by viewModels()
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            searchResultService = (service as SearchResultService.SearchResultBinder).getService()
            searchResultService.registerCallback(viewModel.callback)
            isBound = true
            getCoinList()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
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

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                toCoinSearch()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarSearchResult.buttonToolbarNavigation.setOnClickListener {
            toCoinSearch()
        }
        with(args.condition) {
            binding.textviewSearchResultCondition.text =
                makeConditionString(
                    MyAlarm(
                        0,
                        "",
                        minute,
                        indicator,
                        candle,
                        stochasticK,
                        stochasticD,
                        macdM,
                        value,
                        valueCondition,
                        signal,
                        signalCondition,
                        true
                    ),
                    resources.getStringArray(R.array.indicator_items),
                    resources.getStringArray(R.array.up_down_items),
                    resources.getStringArray(R.array.cross_items)
                )
        }
        initRecyclerView()
        initCollector()
        initService()

        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
    }

    private fun toCoinSearch() {
        System.currentTimeMillis().let { currentTime ->
            if (isBound && currentTime > backPressedTime + FINISH_TIME_OUT) {
                AnimationUtils.loadAnimation(requireContext(), R.anim.animation_shake).let {
                    binding.constraintlayoutSearchResult.startAnimation(it)
                }
                backPressedTime = currentTime
                Toast.makeText(
                    requireContext(),
                    "한 번 더 누르면 검색이 종료 됩니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                findNavController().navigate(R.id.action_searchResultFragment_to_coinSearchFragment)
            }
        }
    }

    private fun initCollector() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchList.collectLatest {
                    searchResultAdapter.submitList(it)
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchCount.collectLatest {
                    val progress = (it / viewModel.totalCount.value.toDouble() * 100).toInt()
                    binding.textviewSearchResultProgress.text = getString(
                        R.string.search_result_toolbar_progress,
                        progress,
                        viewModel.totalCount.value,
                        it
                    )
                    if (it == viewModel.totalCount.value) {
                        removeService()
                    }
                }
            }
        }
    }

    private fun initService() {
        Intent(requireActivity(), SearchResultService::class.java).let {
            it.putExtra("condition", args.condition)
            requireActivity().bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun getCoinList() {
        lifecycleScope.launch {
            coinList.first {
                it.sorted().let { sortedList ->
                    viewModel.setTotalCount(sortedList.size)
                    // 하나씩 검사 시작
                    searchResultService.getStarted(sortedList)
                }
                true
            }
        }
    }

    private fun initRecyclerView() {
        searchResultAdapter = CoinListAdapter { }.apply {
            submitList(emptyList())
        }
        val divider = DividerItemDecoration(requireContext(), LinearLayout.VERTICAL).apply {
            setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider_coin_list)!!)
        }
        binding.recyclerSearchResult.apply {
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(divider)
            adapter = searchResultAdapter
        }
    }

    private fun removeService() {
        requireActivity().unbindService(connection)
        backPressedCallback.remove()
        isBound = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isBound) {
            removeService()
        }
        _binding = null
    }

    companion object {
        private const val FINISH_TIME_OUT = 2500
    }
}
