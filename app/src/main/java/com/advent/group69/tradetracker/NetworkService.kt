package com.advent.group69.tradetracker

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
import com.advent.group69.tradetracker.BatteryAwareness.wentThroughFirstTimeFalseAlarm
import com.advent.group69.tradetracker.BatteryAwareness.notifiedOfPowerSaving
import com.advent.group69.tradetracker.model.SnoozeManager

class NetworkService : Service() {
    private var runTargetScan = true
    private lateinit var serviceLooper: Looper
    private lateinit var serviceHandler: ServiceHandler
    private var updateFrequencyPreference: Long = 8000
    private val targetScanThread = HandlerThread("TutorialService",
            Process.THREAD_PRIORITY_BACKGROUND)
    var stockScanner = StockScanner(this@NetworkService)

    /**
     * Avoids CPU blocking by creating background handler [serviceHandler] for the service
     * Starts the new handler thread [targetScanThread] and then service [serviceLooper]
     */
    override fun onCreate() {
        super.onCreate()
        toast("scanning")

        if (!stockScanner.isRunning) {
            Log.v("NetworkService", "stockScanner.isRunning == false, so starting up service.")
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

        return Service.START_NOT_STICKY
    }

    /**
     * Interrupts [targetScanThread] and cancels [stockScanner]
     */
    override fun onDestroy() {
        Log.i("NetworkService", "onDestroy called")
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
            var secondsSinceScanStarted = 0
            var iteration = 0
            val snoozeMsecInterval = 1000L
            val logger = Logger(this@NetworkService, "Updaten.txt")
            stockScanner.isRunning = true

            while (!currentThread().isInterrupted) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag")
                if (SnoozeManager.isSnoozing()) {
                    wakeLock.acquire(60000)
                    Utility.sleepWithThreadInterruptIfWokenUp(snoozeMsecInterval)
                    wakeLock.release()
                } else {
                    Log.i("NetworkService", "HandleMessage call stockScanner.scanNetwork() iteration #" + ++iteration)
                    wakeLock.acquire(60000)

                    logger.logHowLongItTakesToRun { stockScanner.scanNetwork() }
                    Utility.sleepWithThreadInterruptIfWokenUp(updateFrequencyPreference)

                    wakeLock.release()
                }
                secondsSinceScanStarted++
            }

            toast("Scan stopped after $secondsSinceScanStarted iterations")
            stockScanner.isRunning = false

            //this would stop the service on it's own. we don't want that unless the user toggles scanning button to off
            stopSelf(msg.arg1)
            return
        }
    }
}
