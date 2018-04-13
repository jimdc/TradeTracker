package com.advent.group69.tradetracker.model

import io.reactivex.Observable
import com.advent.group69.tradetracker.model.StockRestService.CryptoModel
import com.advent.group69.tradetracker.stockRestService

class DataModel : IDataModel {
    override fun getCryptoPrice(tickerName: String) = stockRestService.cryptoPrice(tickerName)
}