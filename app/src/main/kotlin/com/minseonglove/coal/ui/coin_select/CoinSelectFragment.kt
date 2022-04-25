package com.minseonglove.coal.ui.coin_select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.FragmentCoinSelectBinding
import kotlinx.coroutines.launch

class CoinSelectFragment : Fragment() {

    private lateinit var _binding: FragmentCoinSelectBinding
    private lateinit var coinSelectAdapter: CoinSelectAdapter

    private val binding get() = _binding
    private val viewModel: CoinSelectViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate<FragmentCoinSelectBinding?>(
            inflater,
            R.layout.fragment_coin_select,
            container,
            false
        ).apply {
            vm = viewModel
            lifecycleOwner = this@CoinSelectFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getCoinList()
        // 코인 목록 가져오기
        lifecycleScope.launch {
            viewModel.coinList.collect {
                initRecyclerView(it)
            }
        }
    }

    private fun initRecyclerView(list: List<String>) {
        coinSelectAdapter = CoinSelectAdapter(list)
        val divider = DividerItemDecoration(requireContext(), VERTICAL).apply {
            setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider_coin_list)!!)
        }
        binding.recyclerCoinselect.apply {
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(divider)
            adapter = coinSelectAdapter
        }
    }

}