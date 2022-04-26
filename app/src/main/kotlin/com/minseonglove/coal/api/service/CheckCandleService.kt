package com.minseonglove.coal.api.service

import com.minseonglove.coal.api.dto.CandleInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CheckCandleService {
    @GET("candles/minutes/{minutes}")
    suspend fun checkCandle(
        @Path("minutes") minutes: Int,
        @Query("market") market: String,
        @Query("count") count: Int
    ): Response<List<CandleInfo>>
}
