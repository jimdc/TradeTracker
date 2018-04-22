package com.advent.tradetracker.model

import com.advent.tradetracker.model.StockRestService.CryptoModel
import io.reactivex.Single

interface IDataModel {
    fun getCryptoPrice(tickerName: String): Single<CryptoModel.Result>
}