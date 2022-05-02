package com.minseonglove.coal.ui.search_result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minseonglove.coal.service.SearchResultService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchResultViewModel : ViewModel() {

    private val _totalCount = MutableStateFlow(1)
    private val _searchCount = MutableStateFlow(0)
    private val _searchList = MutableStateFlow<List<String>>(listOf())

    val totalCount: StateFlow<Int> get() = _totalCount
    val searchCount: StateFlow<Int> get() = _searchCount
    val searchList: StateFlow<List<String>> get() = _searchList

    val callback = object : SearchResultService.ICallBack {
        override fun updateCount(count: Int) {
            viewModelScope.launch {
                _searchCount.emit(count)
            }
        }

        override fun updateList(list: List<String>) {
            viewModelScope.launch {
                _searchList.emit(list)
            }
        }
    }

    fun setTotalCount(count: Int) {
        _totalCount.value = count
    }
}