package com.minseonglove.coal.api.repository

import com.minseonglove.coal.api.dto.CandleInfo
import com.minseonglove.coal.api.service.CheckCandleService
import retrofit2.Response
import javax.inject.Inject

class CheckCandleRepository @Inject constructor(
    private val service: CheckCandleService
) {
    suspend fun checkCandle(
        minutes: Int,
        market: String,
        count: Int
    ): Response<List<CandleInfo>> {
        return service.checkCandle(minutes, market, count)
    }
}