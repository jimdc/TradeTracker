package com.advent.tradetracker

import com.advent.tradetracker.model.DataModel
import java.io.IOException
import java.lang.NumberFormatException
import java.net.UnknownHostException

object StockDownloader {

    private const val INTERNET_EXCEPTION = -2.0
    private const val DOUBLE_CONVERSION_ERROR = -4.0
    private const val OTHER_EXCEPTION = -6.0

    private val dataModel = DataModel()
    fun getCryptoPrice(ticker: String): Double {
        return try {
            dataModel.getCryptoPrice(ticker.toUpperCase())
                    .map { x -> x.USD }
                    .blockingGet()
        } catch (ie: IOException) {
            INTERNET_EXCEPTION
        }
        catch (ie: java.lang.RuntimeException) {
            return OTHER_EXCEPTION
        }
        catch (e: UnknownHostException){
            return OTHER_EXCEPTION
        }
        catch (e2: Exception){
            return OTHER_EXCEPTION
        }
    }

    fun getStockPrice(ticker: String): Double {
        return try {
            dataModel.getStockPrice(ticker.toLowerCase())
                    .map { x -> x.lastSale }
                    .map { x -> x.toDouble() } //remove the $ symbol.. JK
                    .blockingGet()
        } catch (ie: IOException) {
            INTERNET_EXCEPTION
        } catch (dce: NumberFormatException) {
            DOUBLE_CONVERSION_ERROR
        }
        catch (ie: java.lang.RuntimeException) {
            return OTHER_EXCEPTION
        }
        catch (e: UnknownHostException){
            return OTHER_EXCEPTION
        }
        catch (e2: Exception){
            return OTHER_EXCEPTION
        }
    }
}