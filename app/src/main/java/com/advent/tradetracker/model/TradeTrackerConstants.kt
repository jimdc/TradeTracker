package com.advent.tradetracker.model

/**
 * Defined here instead of in [StockRestService] so it can be modified by [StockRestServiceTest]
 */
object TradeTrackerConstants {
    var CRYPTO_BASE_URL = "https://min-api.cryptocompare.com/data/"
    var ALPHA_BASE_URL = "https://www.alphavantage.co/"
}