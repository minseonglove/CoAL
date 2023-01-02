package com.minseonglove.coal.ui.coin.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class CoinSelectViewModel : ViewModel() {

    val textFilter = MutableStateFlow("")
    private val originalList = MutableStateFlow<List<String>>(listOf())
    private val _searchedList = MutableSharedFlow<List<String>>()

    val searchedList: SharedFlow<List<String>> get() = _searchedList

    fun filteringCoin() {
        viewModelScope.launch {
            if (textFilter.value.isEmpty()) {
                _searchedList.emit(originalList.value)
            } else {
                _searchedList.emit(
                    originalList.value.filter { coin ->
                        coin.contains(textFilter.value, true)
                    }
                )
            }
        }
    }

    fun setOriginalList(list: List<String>) {
        originalList.value = list
    }
}
