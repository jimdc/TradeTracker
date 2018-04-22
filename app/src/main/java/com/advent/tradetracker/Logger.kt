package com.advent.tradetracker

import android.content.Context
import android.util.Log
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

class Logger(val context: Context, val fileName: String) {

    fun howLongDoesItTakeToRun(function: () -> Unit): Long {

        val timeBeforeScan = System.currentTimeMillis()
        function()
        val timeAfterScan = System.currentTimeMillis()
        return timeAfterScan - timeBeforeScan
    }

    fun logHowLongItTakesToRun(function: () -> Unit): Boolean {

        val simpleDate = SimpleDateFormat("MM/dd yyyy, HH:mm:ss", Locale.US)
        val dateString = simpleDate.format(Date())
        val runDuration = howLongDoesItTakeToRun(function)

        return logUpdateTimeToFile("The function $function at $dateString took $runDuration ms to run.\r\n")
    }

    fun logUpdateTimeToFile(lastTime: String): Boolean {

        try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE or Context.MODE_APPEND).use { stream ->
                stream.write(lastTime.toByteArray())
            }
        } catch (fileNotFoundException: FileNotFoundException) {
            Log.d("Logger", fileNotFoundException.toString())
            return false
        }
        Log.i("Logger", "wrote: $lastTime")
        return true
    }

}