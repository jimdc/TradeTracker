package com.advent.group69.tradetracker.model

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface StockRestService {

    /**
     * Equivalent to https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=USD
     */

    @GET("price")
    fun cryptoPrice(
            @Query("fsym") cryptoTicker: String,
            @Query("tsyms") realLifeCurrency: String = "USD"
    ):
            Observable<CryptoModel.Result>

    /**
     * {"USD":65.59}
     */
    object CryptoModel {
        data class Result(val USD: Double)
    }

    companion object {
        fun create(): StockRestService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .baseUrl("https://min-api.cryptocompare.com/data/")
                    .build()

            return retrofit.create(StockRestService::class.java)
        }
    }
}