package com.advent.tradetracker.model

import com.advent.tradetracker.model.StockRestService.CryptoModel
import io.reactivex.Single

interface IDataModel {
    fun getCryptoPrice(tickerName: String): Single<CryptoModel.Result>
    fun getStockPrice(tickerName: String): Single<NASDAQService.NASDAQPage>

    fun getCryptoPriceAV(tickerName: String): Single<AlphaVantageService.CurrencyModel.Result>
    fun batchStockPriceAV(tickerName: String): Single<AlphaVantageService.BatchStockModel.StockQuotes>
}