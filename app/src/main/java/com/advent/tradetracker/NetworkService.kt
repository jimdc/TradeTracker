package com.advent.tradetracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import org.jetbrains.anko.toast
import android.os.Looper
import android.os.*
import android.os.Process
import android.os.HandlerThread
import java.lang.Thread.*
import android.support.v7.preference.PreferenceManager

import android.util.Log
import com.advent.tradetracker.BatteryAwareness.notifiedOfPowerSaving
import com.advent.tradetracker.model.SnoozeManager
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*
import com.advent.tradetracker.BatteryAwareness.wentThroughFirstTimeFalseAlarm
import com.advent.tradetracker.Utility.sleepWithThreadInterruptIfWokenUp
import com.advent.tradetracker.Utility.withDollarSignAndDecimal
import com.advent.tradetracker.view.PriceAlertBroadcastReceiver


import timber.log.Timber


class NetworkService : Service() {
    private var runTargetScan = true
    private lateinit var serviceLooper: Looper
    private lateinit var serviceHandler: ServiceHandler
    private var updateFrequencyPreference: Long = 8000
    private val targetScanThread = HandlerThread("TutorialService",
            Process.THREAD_PRIORITY_BACKGROUND)
    var stockScanner = com.advent.tradetracker.StockScanner(this@NetworkService)

    /**
     * Avoids CPU blocking by creating background handler [serviceHandler] for the service
     * Starts the new handler thread [targetScanThread] and then service [serviceLooper]
     */
    override fun onCreate() {
        super.onCreate()
        toast("scanning")
        //displayNotif()

        if (!stockScanner.isRunning) {
            Timber.v( "stockScanner.isRunning == false, so starting up service.")
            stockScanner.isRunning = true
            stockScanner.startup()
            targetScanThread.start()

            serviceLooper = targetScanThread.looper
            serviceHandler = ServiceHandler(serviceLooper)
        }
    }

    /**
     * Get a message instance from [serviceHandler] and send its message
     * @param[startId] is used to identify the service
     * @return START_NOT_STICKY: if the system has to kill it b/c of low memory (or app killed), OK.
     */

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = serviceHandler.obtainMessage()
        message.arg1 = startId
        serviceHandler.sendMessage(message)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        updateFrequencyPreference =
                try {
                    sharedPref.getString(this.resources.getString(R.string.stockupdate_key), "8000").toLong()
                } catch (nfe: NumberFormatException) {
                    8000
                }

        return Service.START_STICKY
    }

    /**
     * Interrupts [targetScanThread] and cancels [stockScanner]
     */
    override fun onDestroy() {
        Timber.i( "onDestroy called")
        runTargetScan = false
        targetScanThread.interrupt()
        if (stockScanner.isRunning) {
            stockScanner.isRunning = false
            stockScanner.cleanup()
        }
        toast("Stopping scan")
        notifiedOfPowerSaving = false
        wentThroughFirstTimeFalseAlarm = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        /**
         * Vibrates, executes [stockScanner], waits for an interrupt, then shuts down
         * We are invoked when [onStartCommand] calls [serviceHandler]'s sendMessage
         */
        override fun handleMessage(msg: Message) {
            // Add your cpu-blocking activity here
            var iterations = 0
            var iteration = 0
            val snoozeMsecInterval = 1000L

            val powerManager = this@NetworkService.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLocker = BatteryAwareness.WakeLocker(powerManager)

            stockScanner.isRunning = true

            while (stockScanner.isRunning) {
                if (SnoozeManager.isSnoozing()) {
                    wakeLocker.wakeLockDo(60000, {
                        sleepWithThreadInterruptIfWokenUp(
                                snoozeMsecInterval
                        )
                    })
                } else {
                    Timber.i( "HandleMessage call stockScanner.scanNetwork() iteration #" + ++iteration)
                    wakeLocker.wakeLockDo(60000, {
                        stockScanner.scanNetwork()
                        sleepWithThreadInterruptIfWokenUp(
                                updateFrequencyPreference
                        )
                    })
                }
                iterations++
            }

            toast("Scan stopped after $iterations iterations")
            stockScanner.isRunning = false
            stopSelf(msg.arg1)
        }
    }
    fun displayNotif(){
        val alertTime = GregorianCalendar().timeInMillis + 5

        val alertIntent = Intent(this@NetworkService, PriceAlertBroadcastReceiver::class.java)

        val alarmManager = this@NetworkService.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                alertTime,
                PendingIntent
                        .getBroadcast(this@NetworkService, 1, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT))
    }
}
