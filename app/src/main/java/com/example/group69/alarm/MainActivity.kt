package com.example.group69.alarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.util.Log
import org.jetbrains.anko.db.*
import org.jetbrains.anko.*

import java.util.GregorianCalendar
import android.content.BroadcastReceiver
import android.support.v4.content.LocalBroadcastManager
import android.content.IntentFilter

// NotificationManager : Allows us to notify the user that something happened in the background
// AlarmManager : Allows you to schedule for your application to do something at a later date
// even if it is in the background

class MainActivity : AppCompatActivity() {

    val showNotificationBut: Button by lazy {  findViewById(R.id.showNotificationBut) as Button }
    val addstockButton: Button by lazy { findViewById(R.id.addstockButton) as Button }
    val showstockButton: Button by lazy { findViewById(R.id.showstockButton) as Button }
    lateinit var stopNotificationBut: Button
    lateinit var alertButton: Button
    lateinit var experiButton: Button
    var stocksTargets : List<Stock> = ArrayList()
    var stocksCurrent : List<Stock> = ArrayList()
    // Allows us to notify the user that something happened in the background
    internal lateinit var notificationManager: NotificationManager

    // Used to track notifications
    internal var notifID = 33
    val updat = Updaten(this)
    // Used to track if notification is active in the task bar
    internal var isNotificActive = false
    var resultReceiver = createBroadcastReceiver()
    override fun onCreate(savedInstanceState: Bundle?) { //Only called when you restore from a saved state!
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize buttons
        stopNotificationBut = findViewById(R.id.stopNotificationBut) as Button
        alertButton = findViewById(R.id.alertButton) as Button
        experiButton = findViewById(R.id.experiButton) as Button

        LocalBroadcastManager.getInstance(this).registerReceiver(resultReceiver, IntentFilter("com.example.group69.alarm"))
        Log.d("dictionary","")
        runOnUiThread {
            try {
                database.use {
                    val sresult = select("Portefeuille", "_stockid", "ticker", "target", "ab", "phone")

                    sresult.exec() {
                        if (count > 0) {
                            val parser = rowParser {
                                stockid: Long, ticker: String, target: Double, above: Long, phone: Long ->
                                Stock(stockid, ticker, target, above, phone)
                            }
                            stocksTargets = parseList(parser)

                        }
                    }
                }
            } catch (e: android.database.sqlite.SQLiteException) {
                toast("error onCreate: " + e.toString())

            }
            if (!stocksTargets.isEmpty()) {

                var stocknamelist: List<CharSequence> = ArrayList()
                stocksTargets.forEach { i -> stocknamelist += i.toString() }
                Log.d("yeezy stocksTargets: ", stocksTargets.toString())

            } else {
                toast("error onCreate, might be empty list: " + getResources().getString(R.string.failempty))
            }


            updat.execute("hi")
            //activates doInBackground
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
    }


    fun showNotification(view: View) {

        // Builds a notification
        val notificBuilder = NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.msg))
                .setContentText(getResources().getString(R.string.newmsg))
                .setTicker(getResources().getString(R.string.alnew))
                .setSmallIcon(R.drawable.ntt_logo_24_24)

        // Define that we have the intention of opening MoreInfoNotification
        val moreInfoIntent = Intent(this, MoreInfoNotification::class.java)

        // Used to stack tasks across activites so we go to the proper place when back is clicked
        val tStackBuilder = TaskStackBuilder.create(this)

        // Add all parents of this activity to the stack
        tStackBuilder.addParentStack(MoreInfoNotification::class.java)

        // Add our new Intent to the stack
        tStackBuilder.addNextIntent(moreInfoIntent)

        // Define an Intent and an action to perform with it by another application
        // FLAG_UPDATE_CURRENT : If the intent exists keep it but update it if needed
        val pendingIntent = tStackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT)

        // Defines the Intent to fire when the notification is clicked
        notificBuilder.setContentIntent(pendingIntent)

        // Gets a NotificationManager which is used to notify the user of the background event
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Post the notification
        notificationManager.notify(notifID, notificBuilder.build())

        // Used so that we can't stop a notification that has already been stopped
        isNotificActive = true


    }

    fun stopNotification(view: View) {

        // If the notification is still active close it
        if (isNotificActive) {
            notificationManager.cancel(notifID)
        }

    }

    fun openHot(view: View) {
        val intent = Intent(this,HotStockActivity::class.java)
        startActivity(intent)
    }


    fun addstock(view: View) {
        val intent = Intent(this, AddEditStockActivity::class.java)
        intent.putExtra("EditingExisting", false);
        startActivity(intent)
    }

    fun showstocks(view: View) {
        var stocklist : List<Stock> = ArrayList()
        try {
            database.use {
                val sresult = select("Portefeuille", "_stockid", "ticker", "target", "ab", "phone")
                sresult.exec() {
                    if (count > 0) {
                        val parser = rowParser {
                            stockid: Long, ticker: String, target: Double, above: Long, phone: Long ->
                            Stock(stockid, ticker, target, above, phone)
                        }

                        stocklist = parseList(parser)
                    }
                }
            }
        } catch (e: android.database.sqlite.SQLiteException) {
            toast(e.toString())
            return
        }

        if (!stocklist.isEmpty()) {

            var stocknamelist : List<CharSequence> = ArrayList()
            stocklist.forEach {
                i -> stocknamelist += i.toString()
            }

            selector(getResources().getString(R.string.choose1), stocknamelist) {
                i -> run {
                    val st0ck = stocklist.get(i)

                    val intent = Intent(this, AddEditStockActivity::class.java)
                    intent.putExtra("EditingExisting", true)
                    intent.putExtra("TheStock", st0ck)

                    startActivity(intent)
                }
            }
        } else {
            toast(getResources().getString(R.string.failempty))
        }
    }

    fun setAlarm(view: View) {

        // Define a time value of 5 seconds
        val alertTime = GregorianCalendar().timeInMillis + 5 * 1000

        // Define our intention of executing AlertReceiver
        val alertIntent = Intent(this, AlertReceiver::class.java)

        // Allows you to schedule for your application to do something at a later date
        // even if it is in he background or isn't active
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // set() schedules an alarm to trigger
        // Trigger for alertIntent to fire in 5 seconds
        // FLAG_UPDATE_CURRENT : Update the Intent if active
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))

    }

    fun playAlarm() {

        Log.d("playAlarm","playAlarm")
        // Define a time value of 5 seconds
        val alertTime = GregorianCalendar().timeInMillis + 5

        // Define our intention of executing AlertReceiver
        val alertIntent = Intent(this, AlertReceiver::class.java)
        val alert1 : String
        alert1 = "ayyyy"
        val alert2 : String
        alert2 = "ayyyy2"
        val alert3 : String
        alert3 = "ayyyy3"
        alertIntent.putExtra("message1", alert1)
        alertIntent.putExtra("message2", alert2)
        alertIntent.putExtra("message3", alert3)
        // Allows you to schedule for your application to do something at a later date
        // even if it is in he background or isn't active
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // set() schedules an alarm to trigger
        // Trigger for alertIntent to fire in 5 seconds
        // FLAG_UPDATE_CURRENT : Update the Intent if active
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))

    }

    private fun createBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("beforeAlarm","mangracina55")
                //deleteStockOfThisIndex(intent.getStringExtra("result"))
            }
        }
    }

    override fun onDestroy() {
        if (resultReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(resultReceiver)
        }
        super.onDestroy()
    }
    fun deleteStockOfThisIndex(s : String){

    }
}