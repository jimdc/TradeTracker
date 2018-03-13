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
import android.os.Vibrator
import android.support.v7.preference.PreferenceManager
import android.util.Log
import org.jetbrains.anko.async
import java.io.File
import java.io.FileInputStream

class
MainService : Service() {
    var runTargetScan = true
    private lateinit var mServiceLooper: Looper
    private lateinit var mServiceHandler: ServiceHandler
    private var updateFrequencyPref: Long = 8000
    private val targetScanThread = HandlerThread("TutorialService",
            Process.THREAD_PRIORITY_BACKGROUND)
    var updat = Updaten(this@MainService)

    /**
     * Avoids CPU blocking by creating background handler [mServiceHandler] for the service
     * Starts the new handler thread [targetScanThread] and then service [mServiceLooper]
     */
    override fun onCreate() {
        super.onCreate()
        toast("scanning")

        if (!updat.running) {
            Log.v("MainService", "updat.running == false, so starting up service.")
            updat.running = true
            targetScanThread.start()

            mServiceLooper = targetScanThread.looper
            mServiceHandler = ServiceHandler(mServiceLooper)
        }
    }

    /**
     * Get a message instance from [mServiceHandler] and send its message
     * @param[startId] is used to identify the service
     * @return START_NOT_STICKY: if the system has to kill it b/c of low memory (or app killed), OK.
     */

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = mServiceHandler.obtainMessage()
        message.arg1 = startId
        mServiceHandler.sendMessage(message)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        updateFrequencyPref =
                try {
                    sharedPref.getString(this.resources.getString(R.string.stockupdate_key), "8000").toLong()
                } catch (nfe: NumberFormatException) {
                    8000
                }

        return Service.START_NOT_STICKY
    }

    /**
     * Interrupts [targetScanThread] and cancels [updat]
     */
    override fun onDestroy() {
        Log.i("MainService", "onDestroy called")
        runTargetScan = false
        targetScanThread.interrupt()
        if (updat.running) updat.running = false
        toast("Stopping scan")
        notifiedOfPowerSaving = false
        notif1 = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        /**
         * Vibrates, executes [updat], waits for an interrupt, then shuts down
         * We are invoked when [onStartCommand] calls [mServiceHandler]'s sendMessage
         */
        override fun handleMessage(msg: Message) {
            // Add your cpu-blocking activity here
            var SecondsSinceScanStarted = 0
            var iteration = 0
            updat.running = true

            while (!currentThread().isInterrupted) {

                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag")
                if (isSnoozing) {
                    if(wakeLock.isHeld) {
                        wakeLock.release()
                    }
                    wakeLock.acquire(60000)
                    Utility.TryToSleepFor(snoozeMsecInterval)
                    snoozeMsecElapsed += snoozeMsecInterval
                    if (snoozeMsecElapsed >= snoozeMsecTotal) {
                        isSnoozing = false
                    }


                }
                else {
                    Log.i("MainService", "HandleMessage call updat.scannetwork() iteration #" + ++iteration)

                    wakeLock.acquire(60000)
                    updat.scannetwork()
                    Utility.TryToSleepFor(updateFrequencyPref)
                    wakeLock.release()
                }

                SecondsSinceScanStarted++
            }

            toast("Scan stopped after $SecondsSinceScanStarted iterations")
            updat.running = false

            //this would stop the service on it's own. we don't want that unless the user toggles scanning button to off
            stopSelf(msg.arg1)
            return
        }
    }

    fun Save(lastTime: Long) {
        async {  //whether or not it is an async task it still does not work
            var newCurrent = System.currentTimeMillis()/1000
            var data = (newCurrent - lastTime).toString()

            var logPath = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Logs")
            if (!logPath.exists()) {
                logPath.mkdirs()
            }
            val filename = "saving.txt"

            val file = File(logPath, filename)
            try {
                /*       FileOutputStream(file).use { // no errors but nothing readable
                           it.write(data.toByteArray())
                       }*/
                openFileOutput(filename, Context.MODE_PRIVATE).use {
                    it.write((data + "\n").toByteArray())  } //this is shown on android website but gives system.err

                /* this didn't work either, neither did bufferedWriter and fileWriter
            try {
                val fos = FileOutputStream(filename)
                fos.write((data + "\n").toByteArray())
                fos.close()
            }
            catch (e: Exception) {
                e.printStackTrace()
            } */
            }
            catch (e: Exception){
            }



            Log.d("service", "saved file")
            val inputAsString = FileInputStream(file).bufferedReader().use { it.readText() }
            Log.d("service",inputAsString)
        }
    }
}