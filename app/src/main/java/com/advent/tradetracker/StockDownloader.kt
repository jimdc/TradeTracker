package com.advent.tradetracker

import com.advent.tradetracker.model.AlphaVantageService
import com.advent.tradetracker.model.DataModel
import java.io.IOException
import java.lang.NumberFormatException
import java.net.UnknownHostException

object StockDownloader {

    private const val INTERNET_EXCEPTION = -2.0
    private const val DOUBLE_CONVERSION_ERROR = -4.0
    private const val NULL_POINTER_EXCEPTION = -5.0
    private const val RUNTIME_EXCEPTION = -6.0
    private const val HOST_EXCEPTION = -7.0
    private const val OTHER_EXCEPTION = -8.0

    private val dataModel = DataModel()
    fun getCryptoPrice(ticker: String): Double {
        return try {
            dataModel.getCryptoPrice(ticker.toUpperCase())
                    .map { x -> x.USD }
                    .blockingGet() }
        catch (ie: IOException) { INTERNET_EXCEPTION }
        catch (ie: java.lang.RuntimeException) { RUNTIME_EXCEPTION }
        catch (npe: NullPointerException) { NULL_POINTER_EXCEPTION }
        catch (e: UnknownHostException){ HOST_EXCEPTION }
        catch (e2: Exception){ OTHER_EXCEPTION }
    }

    fun getCryptoPriceAV(tickerName: String): Double {

        val currencyModel = dataModel.getCryptoPriceAV(tickerName.toUpperCase()).blockingGet()
        val RTCER = currencyModel.realTimeCurrencyExchangeRate
        lateinit var exchangeRate: String

        try { exchangeRate = RTCER.exchangeRate }
        catch (npe: NullPointerException) { return NULL_POINTER_EXCEPTION }

        return try { exchangeRate.toDouble() }
        catch (de: NumberFormatException) { DOUBLE_CONVERSION_ERROR }
        catch (ie: IOException) { INTERNET_EXCEPTION }
        catch (ie: java.lang.RuntimeException) { RUNTIME_EXCEPTION }
        catch (e: UnknownHostException){ HOST_EXCEPTION }
        catch (e2: Exception){ OTHER_EXCEPTION }
    }

    fun getStockPricesAV(ticker: String): List<Double> {

        var individualQuoteList: List<AlphaVantageService.BatchStockModel.IndividualQuote> = emptyList()
        try {
            individualQuoteList = dataModel.batchStockPriceAV("MSFT,FB,AAPL")
                    .map { x -> x.individualQuotes }
                    .blockingGet() }
        catch (ie: IOException) { return listOf(INTERNET_EXCEPTION) }
        catch (npe: NullPointerException) { return listOf(NULL_POINTER_EXCEPTION) }
        catch (ie: java.lang.RuntimeException) { return listOf(RUNTIME_EXCEPTION) }
        catch (e: UnknownHostException){ return listOf(HOST_EXCEPTION) }
        catch (e2: Exception){ return listOf(OTHER_EXCEPTION) }

        return try {
            individualQuoteList.map { q -> q.price.toDouble() } }
        catch (dce: NumberFormatException) { listOf(DOUBLE_CONVERSION_ERROR) }
    }

    fun getStockPrice(ticker: String): Double {
        return try {
            dataModel.getStockPrice(ticker.toLowerCase())
                    .map { x -> x.lastSale }

                    .map { x -> x.toDouble() }
                    .blockingGet() }
        catch (de: NumberFormatException) { DOUBLE_CONVERSION_ERROR }
        catch (ie: IOException) { INTERNET_EXCEPTION }
        catch (npe: NullPointerException) { NULL_POINTER_EXCEPTION }
        catch (ie: java.lang.RuntimeException) { RUNTIME_EXCEPTION }
        catch (e: UnknownHostException){ HOST_EXCEPTION }
        catch (e2: Exception){ OTHER_EXCEPTION }
    }
}