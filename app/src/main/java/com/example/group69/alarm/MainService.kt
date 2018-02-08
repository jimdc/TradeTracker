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
    private val targetScanThread = HandlerThread("TutorialService",
            Process.THREAD_PRIORITY_BACKGROUND)
    val updat = Updaten(this@MainService)

    constructor(applicationContext: Context) : super() {
        Log.i("HERE", "here I am!")
    }

    constructor() {}

    /**
     * Avoids CPU blocking by creating background handler [mServiceHandler] for the service
     * Starts the new handler thread [targetScanThread] and then service [mServiceLooper]
     */
    override fun onCreate() {
        super.onCreate()
        toast("Target scanning")

        if (updat.status != AsyncTask.Status.RUNNING) {
            Log.d("got", "starting!")
            targetScanThread.start()

            mServiceLooper = targetScanThread.looper
            mServiceHandler = ServiceHandler(mServiceLooper)
        }
    }

    /**
     * Get a message instance from [mServiceHandler] and send its message
     * @param[startId] is used to identify the service
     * @return START_STICKY: ask OS to restart service with null intent
     */

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = mServiceHandler.obtainMessage()
        message.arg1 = startId
        mServiceHandler.sendMessage(message)

        return Service.START_STICKY
    }

    /*    protected fun showToast(msg: String) {
            //gets the main thread
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                // run this code in the main thread
                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
            }
        }
    */

    /**
     * Interrupts [targetScanThread] and cancels [updat]
     * @todo Something here is not allowed with Kotlin. Is it unnecessary?
     */
    override fun onDestroy() {
        Log.d("geld", "got destroyed")
        runTargetScan = false
        targetScanThread.interrupt()
        if (updat.status == AsyncTask.Status.RUNNING) {
            updat.cancel(true)
        }

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
            val v = this@MainService.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val num: LongArray = longArrayOf(0, 100)
            v.vibrate(num, -1)

            //if(updat.isRunning)
            updat.execute("hi")
            var ii = 0
            while (!currentThread().isInterrupted) {
                //showToast("Service, id: " + msg.arg1) //might decide to use the arg1 msg later
                //if(ii>0 && ii%15 == 0){
                //toast("Target scan has been running for " + ii + " seconds")
                //}
                try {
                    sleep(5000)
                } catch (e: InterruptedException) {
                    currentThread().interrupt()
                }
                ii++
            }

            toast("Scan stopped after $ii seconds")
            updat.cancel(true)

            //this would stop the service on it's own. we don't want that unless the user toggles scanning button to off
            stopSelf(msg.arg1)
            return

        }
    }
}