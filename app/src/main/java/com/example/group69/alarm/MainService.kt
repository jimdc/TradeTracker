package com.example.group69.alarm

/**
 * Created by nick1_000 on 1/4/2018.
 */
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import org.jetbrains.anko.toast
import android.os.Looper
import android.os.*;
import android.os.Process;
import android.os.HandlerThread
import com.example.group69.alarm.R.string.msg
import java.lang.Thread.*
import android.os.Vibrator
import android.util.Log


class MainService : Service{
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

    override fun onCreate() {
        super.onCreate()
        toast("Target scanning")

        // To avoid cpu-blocking, we create a background handler to run our service

        // start the new handler thread
        if(updat.getStatus() != AsyncTask.Status.RUNNING){
            Log.d("got","starting!")
            targetScanThread.start()

            mServiceLooper = targetScanThread.looper
            // start the service using the background handler
            mServiceHandler = ServiceHandler(mServiceLooper)

        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //toast("onStartCommand")

        // call a new service handler. The service ID can be used to identify the service


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
    override fun onDestroy() {
        Log.d("geld","got destroyed")
        runTargetScan = false
        //this is not allowed with kotlin
        targetScanThread.interrupt()
        if(updat.getStatus() == AsyncTask.Status.RUNNING){
            updat.cancel(true)
        }

        toast("Stopping scan")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            // Well calling mServiceHandler.sendMessage(message);
            // from onStartCommand this method will be called.

            // Add your cpu-blocking activity here
            val v = this@MainService.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val num : LongArray = longArrayOf(0,100)
            v.vibrate(num, -1)

            //if(updat.isRunning)
            updat.execute("hi")
            var ii = 0
            while(!currentThread().isInterrupted()){
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

            toast("Scan stopped after " + ii + " seconds")
            updat.cancel(true)



            // the msg.arg1 is the startId used in the onStartCommand,
            // so we can track the running sevice here.

            //this would stop the service on it's own. we don't want that unless the user toggles scanning button to off
            stopSelf(msg.arg1)
            return

        }
    }




}