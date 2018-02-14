package com.example.group69.alarm

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
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
    @Throws(IOException::class)
    @JvmStatic
    fun getStockPrice(ticker: String): Double {
        try {
            val urlConn = URL("http://www.nasdaq.com/symbol/$ticker/real-time").openConnection()
            val inStream = InputStreamReader(urlConn.getInputStream())
            val buff = BufferedReader(inStream as Reader?)

            var line = buff.readLine()
            //Log.v("geldtime",line)
            while (line != null) {
                //if (line.contains("ref_") && line.contains("_l") ) {
                //if (line.contains("itemprop=\"price\"")) {
                if (line.contains("quotes_content_left__LastSale")) {
                    //Log.d("geldline original", line)
                    //var target = line.indexOf("price")+5
                    //line = line.replace(",", "")

                    //line = line.substring(target, target+10/*line.length - 1*/)

                    //line = buff.readLine()


                    val matcher = Pattern.compile("\\d+.\\d+").matcher(line)
                    matcher.find()
                    //Log.d("geldlineee", matcher.group())
                    val i = java.lang.Double.parseDouble(matcher.group())
                    //Log.d("geldlineee", "price = " + i)
                    //if (ticker.equals("dcth")) {
                    //    Log.d("geldlinee", "hereee")
                    //    Log.d("geldlineee", "price = " + i)
                    //}
                    return i
                }


                //Log.d("geldtime",linhe)
                line = buff.readLine()


            }

            //Log.d("Errorlog", "got -1.0 for stock")
            //html tags may have changed OR stock does not exist on nasdaq.com
            return getNotLivePrice(ticker)
        } catch (e: Exception) {
            //Log.d("geldtime33", "exception thrown (probably at price parsing")
            return getNotLivePrice(ticker) //this error code means that the url is invalid
        }
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
     * Looks for NUM.NUM pattern with any number of digits
     * @param[BufferedReader] The HTML string result
     * @return crypto price if successful, [SKIPPEDPARSE_ERROR], [DOUBLE_CONVERSION_ERROR]
     */
    fun parseCryptoPrice(bae: BufferedReader): Double {

        var ret = SKIPPEDPARSE_ERROR;
        val iterator = bae.lineSequence().iterator()

        while(iterator.hasNext()) {
            val matcher = Pattern.compile("\\d+.\\d+").matcher(iterator.next())
            matcher.find()

            try { ret = java.lang.Double.parseDouble(matcher.group())
            } catch (ne: NumberFormatException) { ret = DOUBLE_CONVERSION_ERROR }

            if (ret != DOUBLE_CONVERSION_ERROR) return ret
        }

        return ret
    }

    /**
     * As a second resort, check NASDAQ's symbol site, but not the "real-time" one.
     * @param[ticker] The ticker name, e.g. GOOG
     * @return stock price if successful, -1 if not found in HTML, -3 if problem with connecting
     * @sample getStockPrice
     */
    fun getNotLivePrice(ticker: String): Double {
        val tickerU = ticker.toUpperCase()
        //Log.d("geldticker", tickerU)
        try {
            val u = "http://www.nasdaq.com/symbol/" + ticker
            val url = URL(u)
            val urlConn = url.openConnection()
            val inStream = InputStreamReader(urlConn.getInputStream())
            val buff = BufferedReader(inStream as Reader?)
            val price = "not found"
            var line = buff.readLine()
            //Log.d("geldtime33", line)
            while (line != null) {
                //if (line.contains("ref_") && line.contains("_l") ) {
                //if (line.contains("itemprop=\"price\"")) {
                if (line.contains("qwidget_lastsale")) {
                    //Log.d("geldline original", line)
                    //var target = line.indexOf("price")+5
                    //line = line.replace(",", "")

                    //line = line.substring(target, target+10/*line.length - 1*/)

                    //line = buff.readLine()
                    //Log.d("geldlinee",line)
                    val matcher = Pattern.compile("\\d+.\\d+").matcher(line)
                    matcher.find()
                    //Log.d("geldline", matcher.group())
                    return java.lang.Double.parseDouble(matcher.group())
                }


                //Log.d("geldtime", line)
                line = buff.readLine()
            }

            //Log.d("Errorlog", "got -1.0 for stock")
            //html tags may have changed OR stock does not exist on nasdaq.com, now we will check if it is a crypto
            return -1.0

        } catch (e: Exception) {
            return -3.0 //this error code means that the url is invalid
        }
    }
}