package com.advent.group69.tradetracker

import com.advent.group69.tradetracker.Geldmonitor.INTERNET_EXCEPTION
import com.advent.group69.tradetracker.Geldmonitor.parseCryptoPrice
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

object Geldmonitor {

    private val INTERNET_EXCEPTION = -2.0
    private val SKIPPEDPARSE_ERROR = -3.0
    private val DOUBLE_CONVERSION_ERROR = -4.0
    private val JSON_DATA_ERROR = -5.0
    private val IO_ERROR = -6.0

    /**
     * Scrapes the NASDAQ website, parses out stock info
     * @param[ticker] The ticker name, e.g. GOOG
     * @return Stock price if successful
     */
    fun getStockPrice(ticker: String): Double {

        val tickerL = ticker.toLowerCase()

        val ret = try {
            val url = URL("https://www.nasdaq.com/symbol/$tickerL/real-time")
            val urlConn = url.openConnection()
            val inStream = InputStreamReader(urlConn.getInputStream())
            parseLiveStockPrice(BufferedReader(inStream as Reader?))
        } catch (ie: IOException) {
            ie.printStackTrace()
            INTERNET_EXCEPTION
        }

        if ((ret == INTERNET_EXCEPTION) ||
                (ret == SKIPPEDPARSE_ERROR) ||
                (ret == DOUBLE_CONVERSION_ERROR))
            return getLateStockPrice(ticker)

        return ret
    }

    /**
     * Debug function
     * @param[url] The webpage you want to open
     * @return A string of the HTML output
     */
    private fun linez(url: String): String {
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
            parseCryptoPrice(linez
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
        val tickerL = ticker.toLowerCase()

        return try {
            val url = URL("https://www.nasdaq.com/symbol/" + tickerL)
            val urlConn = url.openConnection()
            val inStream = InputStreamReader(urlConn.getInputStream())
            parseLateStockPrice(BufferedReader(inStream as Reader?))
        } catch (ie: IOException) {
            INTERNET_EXCEPTION
        }
    }

    /**
     * Looks for NUM.NUM pattern with any number of digits by default, or applies stricter filter
     * @param[BufferedReader] The HTML string result
     * @param[isStock] checks only a certain line
     * @param[isLatestock] checks only a certain line
     * @return crypto price if successful, [SKIPPEDPARSE_ERROR], [DOUBLE_CONVERSION_ERROR]
     */
    fun parseStockCryptoPrice(bae: BufferedReader, isStock: Boolean, isLatestock: Boolean): Double {

        var ret = SKIPPEDPARSE_ERROR
        val iterator = bae.lineSequence().iterator()

        while(iterator.hasNext()) {

            val line = iterator.next()

            if (isStock) { if (!line.contains("quotes_content_left__LastSale")) continue }
            if (isLatestock) { if (!line.contains("qwidget_lastsale")) continue }

            val matcher = Pattern.compile("\\d+.\\d+").matcher(line)
            matcher.find()


            try { ret = java.lang.Double.parseDouble(matcher.group())
            } catch (ne: NumberFormatException) { ret = DOUBLE_CONVERSION_ERROR }
            catch (me: IllegalStateException) { ret = SKIPPEDPARSE_ERROR }
        }

        return ret
    }

    fun parseCryptoPrice(json: String): Double {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        val jsonAdapter = moshi.adapter(Cryptocurrency::class.java)
        val currenC: Cryptocurrency?

        try {
            currenC = jsonAdapter.fromJson(json)
        } catch (io: IOException) {
            return IO_ERROR
        } catch (jo: JsonDataException) {
            return JSON_DATA_ERROR
        }

        return if (currenC == null)
            SKIPPEDPARSE_ERROR
        else
            currenC.USD
    }

    fun parseLiveStockPrice(bae: BufferedReader): Double {
        return parseStockCryptoPrice(bae, true, false)
    }

    fun parseLateStockPrice(bae: BufferedReader): Double {
        return parseStockCryptoPrice(bae, false, true)
    }
}