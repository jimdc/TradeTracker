package com.advent.group69.tradetracker.model

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NASDAQService {

    /**
     * Equivalent to https://www.nasdaq.com/symbol/goog
     *
     * since it's not something like /?symbol=goog, idk how to do in Retrofit
     * following https://www.thedroidsonroids.com/blog/scraping-web-pages-with-retrofit-jspoon-library
     */

    @GET("symbol")
    fun stockPrice(stockTicker: String):
            Single<CryptoModel.Result>

    /**
     * {"USD":65.59}
     */
    object CryptoModel {
        data class Result(val USD: Double)
    }

    companion object {
        fun create(): NASDAQService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .baseUrl("https://www.nasdaq.com/symbol/")
                    .build()

            return retrofit.create(NASDAQService::class.java)
        }
    }

}