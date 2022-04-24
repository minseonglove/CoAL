package com.minseonglove.coal.ui.coin_search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.FragmentCoinSearchBinding

class CoinSearchFragment : Fragment() {

    private lateinit var _binding: FragmentCoinSearchBinding

    private val binding get() = _binding
    private val viewModel: CoinSearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate<FragmentCoinSearchBinding?>(
            inflater,
            R.layout.fragment_coin_search,
            container,
            false
        ).apply {
            vm = viewModel
            lifecycleOwner = this@CoinSearchFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}