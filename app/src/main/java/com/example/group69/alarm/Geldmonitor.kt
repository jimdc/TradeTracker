package com.example.group69.alarm

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.io.InputStreamReader
import java.io.Reader
import java.lang.NumberFormatException
import java.net.URL
import java.util.regex.Pattern

object Geldmonitor {

    const val INTERNET_EXCEPTION = -2.0
    const val SKIPPEDPARSE_ERROR = -3.0
    const val DOUBLE_CONVERSION_ERROR = -4.0

    /**
     * Scrapes the NASDAQ website, parses out stock info
     * @param[ticker] The ticker name, e.g. GOOG
     * @return Stock price if successful
     */
    @JvmStatic
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

    fun linez(url: String): String {
        val inStream = InputStreamReader(URL(url).openConnection().getInputStream())
        return BufferedReader(inStream).use { it.readText() } as String
    }

    /**
     * Scrapes the cryptocompare website for a particular currency
     * @param[ticker] Name of the currency, e.g. ETH
     * @todo add option to compare crypto to any other crypto by swapping out USD with cryptoUnits (can still be USD)
     * @return crypto price if successful, [INTERNET_EXCEPTION], or exceptions in [parseCryptoPrice]
     */
    fun getCryptoPrice(ticker: String): Double {

        val tickerU = ticker.toUpperCase()

        val ret = try {
            val url = URL("https://min-api.cryptocompare.com/data/price?fsym=${tickerU}&tsyms=USD")
            val urlConn = url.openConnection()
            val inStream = InputStreamReader(urlConn.getInputStream())
            parseCryptoPrice(BufferedReader(inStream as Reader?))
        } catch (ie: IOException) {
            INTERNET_EXCEPTION
        }

        return ret
    }

    /**
     * As a second resort, check NASDAQ's symbol site, but not the "real-time" one.
     * @param[ticker] The ticker name, e.g. GOOG
     * @return stock price if successful, -1 if not found in HTML, -3 if problem with connecting
     * @sample getStockPrice
     */
    fun getLateStockPrice(ticker: String): Double {

        val tickerL = ticker.toLowerCase()

        val ret = try {
            val url = URL("https://www.nasdaq.com/symbol/" + tickerL)
            val urlConn = url.openConnection()
            val inStream = InputStreamReader(urlConn.getInputStream())
            parseLateStockPrice(BufferedReader(inStream as Reader?))
        } catch (ie: IOException) {
            INTERNET_EXCEPTION
        }

        return ret
    }

    /**
     * Looks for NUM.NUM pattern with any number of digits by default, or applies stricter filter
     * @param[BufferedReader] The HTML string result
     * @param[isStock] checks only a certain line
     * @param[isLatestock] checks only a certain line
     * @return crypto price if successful, [SKIPPEDPARSE_ERROR], [DOUBLE_CONVERSION_ERROR]
     */
    fun parseStockCryptoPrice(bae: BufferedReader, isStock: Boolean, isLatestock: Boolean): Double {

        var ret = SKIPPEDPARSE_ERROR;
        val iterator = bae.lineSequence().iterator()

        while(iterator.hasNext()) {

            val line = iterator.next()

            if (isStock) { if (!line.contains("quotes_content_left__LastSale")) continue }
            if (isLatestock) { if (!line.contains("qwidget_lastsale")) continue }

            val matcher = Pattern.compile("\\d+.\\d+").matcher(line)
            matcher.find()

            try { ret = java.lang.Double.parseDouble(matcher.group())
            } catch (ne: NumberFormatException) { ret = DOUBLE_CONVERSION_ERROR }

            if (ret != DOUBLE_CONVERSION_ERROR) return ret
        }

        return ret
    }

    fun parseCryptoPrice(bae: BufferedReader): Double {
        return parseStockCryptoPrice(bae, false, false)
    }

    fun parseLiveStockPrice(bae: BufferedReader): Double {
        return parseStockCryptoPrice(bae, true, false)
    }

    fun parseLateStockPrice(bae: BufferedReader): Double {
        return parseStockCryptoPrice(bae, false, true)
    }
}