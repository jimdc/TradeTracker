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
import android.support.design.widget.Snackbar

import java.util.GregorianCalendar
import android.content.ContentValues
import android.view.ViewManager


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

    // Allows us to notify the user that something happened in the background
    internal lateinit var notificationManager: NotificationManager

    // Used to track notifications
    internal var notifID = 33

    // Used to track if notification is active in the task bar
    internal var isNotificActive = false

    internal class Updaten : android.os.AsyncTask<String, String, Int>() {

        var result: Double? = null

        override fun doInBackground(vararg tickers: String): Int? {
            while (true) {
                for (ticker in tickers) {
                    val stock = yahoofinance.YahooFinance.get(ticker);
                    val price = stock.quote.price
                    val change = stock.quote.changeInPercent

                    publishProgress(ticker, price.toString(), change.toString())
                }

                try {
                    Thread.sleep(2000)
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                    Thread.currentThread().interrupt()
                }
            }
        }

        override fun onProgressUpdate(vararg progress: String) {
            Log.d("Stock", progress[0] + " price: " + progress[1] + "pctchange" + progress[2])
        }

        inner class MyUndoListener : View.OnClickListener {

            override fun onClick(v: View) {

                // Code to undo the user's last action
            }
        }

        override fun onPostExecute(result: Int?) {
            //showDialog("Downloaded " + result + " bytes");
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) { //Only called when you restore from a saved state!
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize buttons
        stopNotificationBut = findViewById(R.id.stopNotificationBut) as Button
        alertButton = findViewById(R.id.alertButton) as Button
        experiButton = findViewById(R.id.experiButton) as Button

        Updaten().execute("SWN", "FB") //activates doInBackground
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

    fun experiMent(view: View) {
    }

    fun addstock(view: View) {

        var above : android.widget.RadioButton? = null;

        var vl = verticalLayout {
            val ticker = editText() { hint = "Ticker name"; requestFocus() }
            val tprice = editText() { hint = "Target price" }
            val phone = checkBox { text = "Phone" }
            val ab = radioGroup {
                above = radioButton { text = "above" }
                val below = radioButton { text = "below" }
                check(1)
            }

            button("Add stock") {
                onClick {
                    val target: Double? = tprice.text.toString().toDoubleOrNull();

                    var newstock =
                            Stock(GregorianCalendar().timeInMillis, ticker.text.toString(), target ?: 6.66,
                                    when(above!!.isChecked) { true -> {1} false -> {0} },
                                    when(phone.isChecked) { true -> {1} false -> {0} } )

                    var rownum : Long = 666
                    database.use {
                        rownum = replace("Portefeuille", null, newstock.ContentValues())
                    }

                    if (rownum != -1L) {
                        toast(getResources().getString(R.string.addsuccess) + "#${rownum}: " + newstock.toString())
                    } else {
                        toast(getResources().getString(R.string.fail2add))
                    }

                    finish()
                }
            }
            
        }

    }

    fun showstocks(view: View) {

        var stocklist : List<Stock> = ArrayList()

        try {
            database.use {
                val sresult = select("Portefeuille", "_stockid", "ticker", "target", "ab", "phone")
                sresult.exec() {
                    if (count > 0) {
                        val rowParser = rowParser(::Stock)
                        stocklist = parseList(rowParser)
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

            selector(getResources().getString(R.string.chooselim), stocknamelist) {
                i -> run {
                    val st0ck = stocklist.get(i)
                    val stockid = st0ck.stockid
                    var nraffected : Int = 0

                    database.use {
                        nraffected = delete("Portefeuille", "_stockid=$stockid")
                    }

                    if (nraffected == 1) {
                        toast(getResources().getString(R.string.numdeleted, st0ck.ticker))
                    } else if (nraffected == 0) {
                        toast(getResources().getString(R.string.delfail))
                    }
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
}
