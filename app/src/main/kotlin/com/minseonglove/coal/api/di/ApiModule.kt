package com.minseonglove.coal.api.di

import com.minseonglove.coal.api.data.Constants.Companion.BASE_URL
import com.minseonglove.coal.api.repository.CheckCandleRepository
import com.minseonglove.coal.api.repository.CoinListRepository
import com.minseonglove.coal.api.service.CheckCandleService
import com.minseonglove.coal.api.service.CoinListService
import com.orhanobut.logger.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    fun provideBaseUrl() = BASE_URL

    @Singleton
    @Provides
    fun provideOkHttpClient() = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    } else {
        OkHttpClient.Builder().build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideCoinListService(retrofit: Retrofit): CoinListService =
        retrofit.create(CoinListService::class.java)

    @Singleton
    @Provides
    fun provideCoinListRepository(service: CoinListService): CoinListRepository =
        CoinListRepository(service)

    @Singleton
    @Provides
    fun provideCheckCandleService(retrofit: Retrofit): CheckCandleService =
        retrofit.create(CheckCandleService::class.java)

    @Singleton
    @Provides
    fun provideCheckCandleRepository(service: CheckCandleService): CheckCandleRepository =
        CheckCandleRepository(service)
}
