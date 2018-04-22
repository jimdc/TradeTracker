package com.advent.tradetracker.future

import android.util.Log
import java.util.*

class MarketOpenTime {

    private var timeCount: Long = 0
    private var isSleepTime = false

    /**
     * isSleepTime true means: scanning is paused and will resume at specified start time, go to settings to change start time
     */
    fun sleepUntilMarketReopens() {

        val date = Date()

        GregorianCalendar().time
        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("America/New_York")
        cal.time = date
        val hours = cal.get(Calendar.HOUR_OF_DAY)
        Log.d("hours", "hours1 " + hours.toString())

        if(isSleepTime) {
            val hour = cal.get(Calendar.HOUR_OF_DAY)

            val starthour = 8
            val startmin = 58

            if (hour in 18..23) {
                Log.d("got sleep", "sleeping for " + (26 - hour) + " hours")
                Thread.sleep((26 - hour).toLong() * 60 * 60000 - 60000 * 5)
            } else if (hour in 0..1) {
                Log.d("got sleep", "sleeping for " + (2 - hour) + " hours")
                Thread.sleep((2 - hour).toLong() * 60 * 60000 - 60000 * 5)
            }

            //at this point, when accounting for DST, time can be 1:00 or 1:59, as well as
            // 3:00 or 3:59 (in which case we wait 1 minute if we wanted the 4am trading
            //if user started scan at say 7 and wants to wait until 9 that will also work
            if (cal.get(Calendar.HOUR_OF_DAY) != starthour + 1
                    || !(cal.get(Calendar.HOUR_OF_DAY) == starthour
                            && cal.get(Calendar.MINUTE) >= startmin)) { //if 8:58 or 8:59.. skip this

                if (cal.get(Calendar.HOUR_OF_DAY) != 8) {
                    Log.d("got sleep", "sleeping for " + (starthour - cal.get(Calendar.HOUR_OF_DAY)) + " hours")
                    Thread.sleep((starthour - cal.get(Calendar.HOUR_OF_DAY)).toLong() * 60 * 60000)
                }

                if (cal.get(Calendar.MINUTE) != 59 && cal.get(Calendar.MINUTE) != 58) {
                    Log.d("got sleep", "sleeping for " + (startmin - cal.get(Calendar.MINUTE)) + " minutes")
                    Thread.sleep((startmin - cal.get(Calendar.MINUTE)).toLong() * 60000 + 10)
                }
            }

            isSleepTime = false
        }

        if(timeCount % 30 == 0L) {
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            if(hour !in 9..17){ //send broadcast or signal for a toast to go off which lets user know that
                isSleepTime = true
            }
        }

        timeCount++
    }
}