package com.advent.tradetracker

import android.util.Log
import com.advent.tradetracker.StockDownloader.INTERNET_EXCEPTION
import com.advent.tradetracker.StockDownloader.parseCryptoPrice
import com.advent.tradetracker.model.Cryptocurrency
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.lang.NumberFormatException
import java.net.URL
import java.util.regex.Pattern

object StockDownloader {

    private const val INTERNET_EXCEPTION = -2.0
    private const val SKIPPED_PARSE_ERROR = -3.0
    private const val DOUBLE_CONVERSION_ERROR = -4.0
    private const val JSON_DATA_ERROR = -5.0
    private const val IO_ERROR = -6.0

    /**
     * Scrapes the NASDAQ website, parses out stock info
     * @param[ticker] The ticker name, e.g. GOOG
     * @return Stock price if successful
     */
    fun getStockPriceLive(ticker: String): Double {

        val tickerLowercase = ticker.toLowerCase()

        val returnValue = try {
            val url = URL("https://www.nasdaq.com/symbol/$tickerLowercase/real-time")
            val urlConn = url.openConnection()
            val inStream = InputStreamReader(urlConn.getInputStream())
            parseLiveStockPrice(BufferedReader(inStream as Reader?))
        } catch (exception: java.io.IOException) {
            exception.printStackTrace()
            INTERNET_EXCEPTION
        }

        when(returnValue)
        {
            INTERNET_EXCEPTION -> return getLateStockPrice(ticker)
            SKIPPED_PARSE_ERROR -> return getLateStockPrice(ticker)
            DOUBLE_CONVERSION_ERROR -> return getLateStockPrice(ticker)
        }

        return returnValue
    }

    /**
     * Debug function
     * @param[url] The webpage you want to open
     * @return A string of the HTML output
     */
    private fun urlHtml(url: String): String {
        val inStream = InputStreamReader(URL(url).openConnection().getInputStream())
        return BufferedReader(inStream).use { it.readText() }
    }

    /**
     * Scrapes the cryptocompare website for a particular currency
     * @param[ticker] Name of the currency, e.g. ETH
     * @todo add option to compare crypto to any other crypto by swapping out USD with cryptoUnits (can still be USD)
     * @return crypto price if successful, [INTERNET_EXCEPTION], or exceptions in [parseCryptoPrice]
     */
    fun getCryptoPrice(ticker: String): Double {
        return try {
            parseCryptoPrice(urlHtml
            ("https://min-api.cryptocompare.com/data/price?fsym=${ticker.toUpperCase()}&tsyms=USD"))
        } catch (ie: IOException) {
            INTERNET_EXCEPTION
        }
    }

    /**
     * As a second resort, check NASDAQ's symbol site, but not the "real-time" one.
     * @param[ticker] The ticker name, e.g. GOOG
     * @return stock price if successful, -1 if not found in HTML, -3 if problem with connecting
     * @sample getStockPrice
     */
    fun getLateStockPrice(ticker: String): Double {
        val tickerLowercase = ticker.toLowerCase()

        return try {
            val url = URL("https://www.nasdaq.com/symbol/$tickerLowercase")
            val urlConnection = url.openConnection()
            val inStream = InputStreamReader(urlConnection.getInputStream())
            parseLateStockPrice(BufferedReader(inStream as Reader?))
        } catch (ie: IOException) {
            INTERNET_EXCEPTION
        }
    }

    /**
     * Looks for NUM.NUM pattern with any number of digits by default, or applies stricter filter
     * @param[source] The HTML string result
     * @param[isStock] checks only a certain line
     * @param[isLateStock] checks only a certain line
     * @return crypto price if successful, [SKIPPED_PARSE_ERROR], [DOUBLE_CONVERSION_ERROR]
     */
    private fun parseStockOrCryptoPrice(source: BufferedReader, isStock: Boolean, isLateStock: Boolean): Double {

        var ret = SKIPPED_PARSE_ERROR
        val iterator = source.lineSequence().iterator()

        while(iterator.hasNext()) {

            val line = iterator.next()

            if (!line.contains("quotes_content_left__LastSale")) continue
            if(!line.contains("display:inline-block")) continue
            //if (isLateStock) { if (!line.contains("qwidget_lastsale")) continue }
            Log.d("stockline",line)
            val matcher = Pattern.compile("\\d+.\\d+").matcher(line)
            matcher.find()
            Log.d("stocko",matcher.group())


            ret = try {
                java.lang.Double.parseDouble(matcher.group())
            } catch (numberFormatException: NumberFormatException) {
                DOUBLE_CONVERSION_ERROR
            } catch (illegalStateException: IllegalStateException) {
                SKIPPED_PARSE_ERROR
            }
        }

        return ret
    }

    fun parseCryptoPrice(json: String): Double {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        val jsonAdapter = moshi.adapter(com.advent.tradetracker.model.Cryptocurrency::class.java)
        val currency: com.advent.tradetracker.model.Cryptocurrency?

        try {
            currency = jsonAdapter.fromJson(json)
        } catch (io: IOException) {
            return IO_ERROR
        } catch (jo: JsonDataException) {
            return JSON_DATA_ERROR
        }
        Log.d("cryptoprice", currency.toString())
        return currency?.USD ?: SKIPPED_PARSE_ERROR
    }

    fun parseLiveStockPrice(source: BufferedReader): Double {
        return parseStockOrCryptoPrice(source, true, false)
    }

    fun parseLateStockPrice(source: BufferedReader): Double {
        return parseStockOrCryptoPrice(source, false, true)
    }
}