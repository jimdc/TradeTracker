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
        var runs: Int = 0

        override fun doInBackground(vararg tickers: String): Int? {
            runs = 0
            Log.d("all tickers", tickers.get(0).toString())
            var s : String = "fb"
            Log.d("all tickers", tickers.get(0).toString() + " " + tickers.get(1).toString())
            while (true) {
                try {

                    for (ticker in tickers) {
                        publishProgress(
                                SyntaxAnalysierer.PriceAndPercent(ticker)
                        )
                    }


                } catch (n: NumberFormatException) {
                    result = -5.0
                    n.printStackTrace()
                    return -1
                }

                try {
                    Thread.sleep(1000)
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                    Thread.currentThread().interrupt()
                }

                runs++
            }
        }

        override fun onProgressUpdate(vararg progress: String) {
            Log.d("SWN price: ", progress[0])
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

        Log.d("Somehow", "I am called")
        Updaten().execute("SWN", "FB") //activates doInBackground
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        var buttonup: String = "Press me"
        /*database.use {
            select("Test_table", "lastID").exec() {
                if (count > 0) {
                    moveToLast()
                    buttonup = getString(0)
                } else {
                    buttonup = "666"
                }
            }
        }*/

        experiButton.text = buttonup
    }

    override fun onPause() {
        super.onPause()
        Log.d("YAY", "I was paused")
        /*database.use {
            replace("Test_table", "lastID" to GregorianCalendar().timeInMillis.toString())
        }*/
    }


    fun showNotification(view: View) {

        // Builds a notification
        val notificBuilder = NotificationCompat.Builder(this)
                .setContentTitle("Message")
                .setContentText("New Message")
                .setTicker("Alert New Message")
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
        val freshthing : String = GregorianCalendar().timeInMillis.toString();
        var result : Long = 666;
        experiButton.text = freshthing
        database.use {

            result = replace("Test_table", "lastID" to freshthing)
        }

        val snack = Snackbar.make(view, "Rows affected: " + result.toString(), 5)
        snack.show()
    }

    fun addstock(view: View) {
        var vl = verticalLayout {
            val ticker = editText() { hint = "Ticker name"; requestFocus() }
            val tprice = editText() { hint = "Target price" }
            val phone = checkBox { text = "Phone" }
            val ab = radioGroup {
                radioButton { text = "above" }
                radioButton { text = "below" }
                check(1) //set "above" to be default
            }

            button("Add stock") {
                onClick {
                    val newstock: Stock = Stock(GregorianCalendar().timeInMillis,
                            ticker.text.toString(), tprice.text.toString().toDoubleOrNull(),
                            (ab.checkedRadioButtonId == 1), phone.isChecked)
                    var rownum : Long = 666

                    database.use {
                        val con = ContentValues()
                        con.put("_stockid", newstock.stockid)
                        con.put("ticker", newstock.ticker)
                        con.put("target", newstock.target)
                        con.put("ab", newstock.above)
                        con.put("phone", newstock.phone)

                        rownum = replace("Portefeuille", null, con)
                    }

                    if (rownum != -1L) {
                        toast("Added row ${rownum}: " + newstock.toString())
                    } else {
                        toast("SQLiteDatabase error, could not add row")
                    }
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
                        val rowParser = classParser<Stock>()
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
            stocklist.forEach { i -> stocknamelist += i.toString() }

            selector("Choose one of your stocks", stocknamelist) {
                i -> toast("You chose option " + i)
            }
        } else {
            toast("DB query failed; stock list is empty")
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
