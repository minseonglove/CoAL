package com.minseonglove.coal.api.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

class Constants {
    companion object {
        const val BASE_URL = "https://api.upbit.com/v1/"
        val Context.datastore: DataStore<Preferences> by preferencesDataStore(
            name = "coin_list"
        )
        val SAVED_COIN_LIST = stringSetPreferencesKey("coin")
    }
}