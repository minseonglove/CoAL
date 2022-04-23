package com.minseonglove.coal.api.service

import com.minseonglove.coal.api.dto.CoinList
import retrofit2.Response
import retrofit2.http.GET

interface CoinListService {
    @GET("market/all")
    suspend fun getCoinList(): Response<List<CoinList>>
}