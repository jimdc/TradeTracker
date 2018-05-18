package com.advent.tradetracker.model

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Single
import pl.droidsonroids.jspoon.annotation.Selector
import pl.droidsonroids.retrofit2.JspoonConverterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NASDAQService {

    @GET("symbol/{ticker}/real-time")
    fun stockPrice(
            @Path("ticker") ticker: String
    ): Single<NASDAQService.NASDAQPage>

    class NASDAQPage {
        @Selector("#quotes_content_left__LastSale")
        var lastSale: String? = null
    }

    companion object {
        fun create(): NASDAQService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(JspoonConverterFactory.create())
                    .baseUrl("https://www.nasdaq.com/")
                    .build()

            return retrofit.create(NASDAQService::class.java)
        }
    }

}