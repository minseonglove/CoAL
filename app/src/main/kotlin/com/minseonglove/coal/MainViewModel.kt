package com.minseonglove.coal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minseonglove.coal.api.repository.CoinListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CoinListRepository
) : ViewModel() {

    private val _loadingCoinList = MutableSharedFlow<Set<String>>()

    val loadingCoinList: SharedFlow<Set<String>> get() = _loadingCoinList

    fun getCoinList(){
        viewModelScope.launch {
            val coinSet: HashSet<String> = HashSet()
            val coinList = repository.getCoinList()
            if (coinList.isSuccessful) {
                coinList.body()?.forEach { coin ->
                    coinSet.add("${coin.koreanName}(${coin.market})")
                }
                _loadingCoinList.emit(coinSet)
            } else {
                Log.d("error", coinList.errorBody().toString())
            }
        }
    }


}