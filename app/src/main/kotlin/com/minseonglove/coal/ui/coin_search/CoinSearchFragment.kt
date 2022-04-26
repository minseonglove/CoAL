package com.minseonglove.coal.ui.coin_search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.FragmentCoinSearchBinding

class CoinSearchFragment : Fragment(R.layout.fragment_coin_search) {

    private val binding by viewBinding(FragmentCoinSearchBinding::bind)
    private val viewModel: CoinSearchViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }
}
