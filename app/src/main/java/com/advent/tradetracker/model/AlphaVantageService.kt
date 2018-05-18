package com.advent.tradetracker.model

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Single
import okhttp3.OkHttpClient
import pl.droidsonroids.jspoon.annotation.Selector
import pl.droidsonroids.retrofit2.JspoonConverterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import timber.log.Timber
import java.net.UnknownHostException

interface AlphaVantageService {

    /**
     * Equivalent to https://www.alphavantage.co/
     *     query?function=BATCH_STOCK_QUOTES&symbols=MSFT,FB,AAPL&apikey=demo
     */

    @GET("query")
    fun batchStockPrice(
            @Query("function") function: String = "BATCH_STOCK_QUOTES",
            @Query("symbols") symbols: String = "MSFT,FB,AAPL",
            @Query("apikey") apiKey: String = "demo"
    ):
            Single<BatchStockModel.StockQuotes>

    /**
     *  Equivalent to https://www.alphavantage.co/
     *      query?function=CURRENCY_EXCHANGE_RATE&from_currency=BTC&to_currency=CNY&apikey=demo
     *
     *  Scratch that.
     *  https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=BTC&to_currency=USD&apikey=UJ5TXUHQG1IV6NZM
     */

    @GET("query?function=CURRENCY_EXCHANGE_RATE&from_currency=BTC&to_currency=USD&apikey=UJ5TXUHQG1IV6NZM")
    fun cryptoPrice(
            /*@Query("function") function: String = "CURRENCY_EXCHANGE_RATE",
            @Query("from_currency") fromCurrency: String = "BTC",
            @Query("to_currency") toCurrency: String = "CNY",
            @Query("apikey") apiKey: String = "demo"*/
    ):
            Single<CurrencyModel.Result>


    /**
     * {"USD":65.59}
     *
     * { "Meta Data":
     *   { "1. Information": "Batch Stock Market Quotes",
     *     "2. Notes": "IEX Real-Time Price provided for free by IEX (https://iextrading.com/developer/).",
     *     "3. Time Zone": "US/Eastern" },
     *
     *     "Stock Quotes": [
    {"1. symbol": "MSFT", "2. price": "96.2400", "3. volume": "12126968", "4. timestamp": "2018-05-17 16:00:00"},
    {"1. symbol": "FB", "2. price": "183.7200", "3. volume": "10879171", "4. timestamp": "2018-05-17 16:00:00"},
    {"1. symbol": "AAPL", "2. price": "187.0000", "3. volume": "14302750", "4. timestamp": "2018-05-17 16:00:00"}
    ]
     *
     */
    object BatchStockModel {
        data class MetaData(val information: String, val notes: String, val timeZone: String)
        data class StockQuotes(val individualQuotes: List<IndividualQuote>)
        data class IndividualQuote(val symbol: String, val price: String, val volume: String, val timestamp: String)
    }

    /**
     *  {
    "Realtime Currency Exchange Rate": {
    "1. From_Currency Code": "BTC",
    "2. From_Currency Name": "Bitcoin",
    "3. To_Currency Code": "CNY",
    "4. To_Currency Name": "Chinese Yuan",
    "5. Exchange Rate": "52250.28570460",
    "6. Last Refreshed": "2018-05-17 21:03:15",
    "7. Time Zone": "UTC"
    }
    }
     */

    object CurrencyModel {
        data class Result(var realTimeCurrencyExchangeRate: RealTimeCurrencyExchangeRate)
        data class RealTimeCurrencyExchangeRate(
                val fromCurrencyCode: String,
                val fromCurrencyName: String,
                val toCurrencyCode: String,
                val toCurrencyName: String,
                val exchangeRate: String,
                val lastRefreshed: String,
                val timeZone: String
        )
    }

    companion object {
        fun create(): AlphaVantageService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .baseUrl(TradeTrackerConstants.ALPHA_BASE_URL)
                    .build()

            return retrofit.create(AlphaVantageService::class.java)
        }
    }

}