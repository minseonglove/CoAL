package com.minseonglove.coal.api.repository

import com.minseonglove.coal.api.dto.CoinList
import com.minseonglove.coal.api.service.CoinListService
import retrofit2.Response
import javax.inject.Inject

class CoinListRepository @Inject constructor(
    private val service: CoinListService
) {
    suspend fun getCoinList(): Response<List<CoinList>> {
        return service.getCoinList()
    }
}
