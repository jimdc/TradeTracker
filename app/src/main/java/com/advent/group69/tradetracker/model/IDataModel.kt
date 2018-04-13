package com.advent.group69.tradetracker.model

import io.reactivex.Observable
import com.advent.group69.tradetracker.model.StockRestService.CryptoModel
import io.reactivex.Single

interface IDataModel {
    fun getCryptoPrice(tickerName: String): Single<CryptoModel.Result>
}