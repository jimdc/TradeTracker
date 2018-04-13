package com.advent.group69.tradetracker.model

import io.reactivex.Observable
import com.advent.group69.tradetracker.model.StockRestService.CryptoModel

interface IDataModel {
    fun getCryptoPrice(tickerName: String): Observable<CryptoModel.Result>
}