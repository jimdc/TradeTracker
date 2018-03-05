package com.example.group69.alarm

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
import android.util.Log


class MainService : Service {
    var runTargetScan = true
    private lateinit var mServiceLooper: Looper
    private lateinit var mServiceHandler: ServiceHandler
    lateinit var mMainActivity: MainActivity
    private val targetScanThread = HandlerThread("TutorialService",
            Process.THREAD_PRIORITY_BACKGROUND)
    var updat = Updaten(this@MainService)

    constructor(callerContext: Context) : super() {
        Log.i("MainService", "Constructor called")
        mMainActivity = callerContext as MainActivity
    }

    constructor() {}

    /**
     * Avoids CPU blocking by creating background handler [mServiceHandler] for the service
     * Starts the new handler thread [targetScanThread] and then service [mServiceLooper]
     */
    override fun onCreate() {
        super.onCreate()
        toast("scanning")

        if (!updat.running) {
            Log.d("got", "starting!")
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

        return Service.START_NOT_STICKY
    }

    /**
     * Interrupts [targetScanThread] and cancels [updat]
     * @todo Something here is not allowed with Kotlin. Is it unnecessary?
     */
    override fun onDestroy() {
        Log.d("geld", "got destroyed")
        runTargetScan = false
        targetScanThread.interrupt()
        if (updat.running) updat.running = false
        toast("Stopping scan")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        /**
         * Vibrates, executes [updat], waits for an interrupt, then shuts down
         * We are invoked when [onStartCommand] calls [mServiceHandler]'s sendMessage
         * @todo track service using [msg.arg1], which is the same as [startId] in [onStartCommand]
         */
        override fun handleMessage(msg: Message) {
            // Add your cpu-blocking activity here
            val VibratorService = this@MainService.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            val VibratorWaitDuration: LongArray = longArrayOf(0, //Millisecond of vibrator duration
                    100) //Milliseconds before turning the vibrator on

            VibratorService.vibrate(VibratorWaitDuration, -1)

            Log.v("MainService", "ServiceHandler starting")

            var SecondsSinceScanStarted = 0
            var iteration = 0
            updat.running = true

            while (!currentThread().isInterrupted) {
                Log.d("MainService", "HandleMessage call updat.scannetwork() iteration #" + ++iteration)
                val t = updat.scannetwork()

                if (isSnooze) {  //a return code of 2 means that snooze was activated and the sleep value was loaded into snoozeTime from mainActivity
                    Log.d("Main","snoozetime")
                    scanRunning = false  //scan refers to updaten, not the service
                    toast("sleeping!" + snoozeTime)
                    Utility.TryToSleepFor(snoozeTime.toLong()*1000)
                    toast("scan resuming")
                    scanRunning = true
                    isSnooze = false
                }
                else Utility.TryToSleepFor(8000)


                SecondsSinceScanStarted++
            }

            toast("Scan stopped after $SecondsSinceScanStarted iterations")
            updat.running = false

            //this would stop the service on it's own. we don't want that unless the user toggles scanning button to off
            stopSelf(msg.arg1)
            return
        }
    }
}