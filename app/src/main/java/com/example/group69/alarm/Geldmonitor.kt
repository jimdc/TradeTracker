package com.example.group69.alarm

import android.util.Log
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

object Geldmonitor {
    @Throws(IOException::class)
    @JvmStatic
    fun getPrice(ticker: String): Double {
        try {
            val url = URL("http://finance.google.com/finance/info?client=ig&q=" + ticker.toUpperCase())
            val urlConn = url.openConnection()
            val inStream = InputStreamReader(urlConn.getInputStream())
            val buff = BufferedReader(inStream)
            val price = "not found"
            var line = buff.readLine()
            while (line != null) {
                if (line!!.contains(",\"l_cur\"")) {
                    Log.d("geld", line)
                    val matcher = Pattern.compile("\\d+.\\d+").matcher(line)
                    matcher.find()
                    val i = java.lang.Double.parseDouble(matcher.group())
                    Log.d("geld2", i.toString())
                    return i;
                }
                line = buff.readLine()

            }
            return -1.0;
        }
        catch(e: Exception){
            return -1.0;
        }
    }
}