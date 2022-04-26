package com.minseonglove.coal

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.minseonglove.coal.api.data.Constants.Companion.SAVED_COIN_LIST
import com.minseonglove.coal.api.data.Constants.Companion.datastore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.getCoinList()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingCoinList.collect { coinList ->
                    saveCoinList(coinList)
                }
            }
        }
    }

    private suspend fun saveCoinList(coinList: Set<String>) {
        applicationContext.datastore.edit { pref ->
            pref[SAVED_COIN_LIST] = coinList
        }
    }
}
