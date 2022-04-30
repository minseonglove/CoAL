package com.minseonglove.coal

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.minseonglove.coal.api.data.Constants.SAVED_COIN_LIST
import com.minseonglove.coal.api.data.Constants.datastore
import com.minseonglove.coal.service.WatchIndicatorService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadCoinName()
        startWatchService()
    }

    private fun startWatchService() {
        if (WatchIndicatorService.service == null) {
            startService(Intent(this, WatchIndicatorService::class.java))
        }
    }

    private fun loadCoinName() {
        viewModel.getCoinList()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingCoinList.first { coinList ->
                    saveCoinList(coinList)
                    true
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
