package com.minseonglove.coal.ui.coin_select

import android.app.Application
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.minseonglove.coal.api.data.Constants
import com.minseonglove.coal.api.data.Constants.Companion.datastore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

class CoinSelectViewModel(application: Application) : AndroidViewModel(application) {

    val textFilter = MutableStateFlow("")
    private val _changeFilter = MutableSharedFlow<String>()

    val changeFilter: SharedFlow<String> get() = _changeFilter
    lateinit var coinList: Flow<List<String>>

    fun getCoinList() {
        viewModelScope.launch {
            getApplication<Application>().applicationContext.apply {
                coinList = datastore.data
                    .catch { exception ->
                        if (exception is IOException) {
                            emit(emptyPreferences())
                        } else {
                            throw exception
                        }
                    }
                    .map {preferences ->
                        preferences[Constants.SAVED_COIN_LIST]?.toList() ?: listOf()
                    }
            }
        }
    }
}