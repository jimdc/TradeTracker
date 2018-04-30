package com.advent.tradetracker

import com.advent.tradetracker.model.DataModel
import java.io.IOException
import java.lang.NumberFormatException

object StockDownloader {

    private const val INTERNET_EXCEPTION = -2.0
    private const val DOUBLE_CONVERSION_ERROR = -4.0

    private val dataModel = DataModel()
    fun getCryptoPrice(ticker: String): Double {
        return try {
            dataModel.getCryptoPrice(ticker.toUpperCase())
                    .map { x -> x.USD }
                    .blockingGet()
        } catch (ie: IOException) {
            INTERNET_EXCEPTION
        }
    }

    fun getStockPrice(ticker: String): Double {
        return try {
            dataModel.getStockPrice(ticker.toLowerCase())
                    .map { x -> x.lastSale }
                    .map { x -> x.substring(1).toDouble() } //remove the $ symbol
                    .blockingGet()
        } catch (ie: IOException) {
            INTERNET_EXCEPTION
        } catch (dce: NumberFormatException) {
            DOUBLE_CONVERSION_ERROR
        }
    }
}