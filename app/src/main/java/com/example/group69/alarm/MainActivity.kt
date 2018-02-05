package com.example.group69.alarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import android.util.Log
import org.jetbrains.anko.db.*
import org.jetbrains.anko.*
import java.util.GregorianCalendar
import android.content.BroadcastReceiver
import android.support.v4.content.LocalBroadcastManager
import android.content.IntentFilter
import android.widget.ListView
import android.app.ActivityManager

///NotificationManager : Allows us to notify the user that something happened in the background
// AlarmManager : Allows you to schedule for your application to do something at a later date
// even if it is in the background

class MainActivity : AppCompatActivity() {

    private var mServiceIntent: Intent? = null
    private var mMainService: MainService? = null
    val servRunning = true
    var stocksList: List<Stock> = ArrayList()
    // Allows us to notify the user that something happened in the background
    internal lateinit var notificationManager: NotificationManager

    // Used to track notifications
    internal var notifID = 33
    //val updat = Updaten(this)

    // Used to track if notification is active in the task bar
    internal var isNotificActive = false
    var resultReceiver = createBroadcastReceiver()

    var listView: ListView? = null
    var adapter: UserListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LocalBroadcastManager.getInstance(this).registerReceiver(resultReceiver, IntentFilter("com.example.group69.alarm"))
        Log.d("dictionary", "")

        stocksList = getStocklistFromDB()

        if (stocksList.isEmpty()) {
            toast("error onCreate, might be empty list: " + resources.getString(R.string.failempty))
        } else {

            var stocknamelist: List<CharSequence> = ArrayList()
            stocksList.forEach { i -> stocknamelist += i.toString() }
            Log.d("yeezy stocksTargets: ", stocksList.toString())

            listView = findViewById<ListView>(R.id.listView)
            adapter = UserListAdapter(this, stocksList)

            listView?.adapter = adapter
            adapter?.notifyDataSetChanged()

            listView?.setOnItemClickListener { parent, view, position, id ->
                alert("Are you sure you want to delete row " + position.toString(), "Confirm") {
                    positiveButton("Yes") {
                        var deleted = deletestock(position)

                        if (deleted) {
                            toast("Row " + position.toString() + " deleted.")
                        } else {
                            toast("Row " + position.toString() + " not deleted.")
                        }
                    }
                    negativeButton("No") {  toast("OK, nothing was deleted.") }
                }.show()
            }
        }
    }

    fun deletestock(position: Int) : Boolean {

        val target = stocksList.get(position) as Stock
        val id = target.stockid
        var success: Boolean = false

        database.use {
            var rez = delete(NewestTableName, "_stockid = ?", arrayOf(id.toString()))

            if (rez > 0) {
                success = true
            }
        }

        if (success) {
            stocksList = getStocklistFromDB()
            adapter?.refresh(stocksList)
        }

        return success
    }

    override fun onResume() {
        super.onResume()
        stocksList = getStocklistFromDB()
        adapter?.refresh(stocksList)
    }

    fun getStocklistFromDB() : List<Stock> {
        var results: List<Stock> = ArrayList()
        try {
            database.use {
                val sresult = select(NewestTableName, "_stockid", "ticker", "target", "ab", "phone", "crypto")

                sresult.exec {
                    if (count > 0) {
                        val parser = rowParser { stockid: Long, ticker: String, target: Double, above: Long, phone: Long, crypto: Long ->
                            Stock(stockid, ticker, target, above, phone, crypto)
                        }
                        results = parseList(parser)
                    }
                }
            }
        } catch (e: android.database.sqlite.SQLiteException) {
            Log.e("err gSlFDB: ", e.toString())
        }

        return results
    }

    fun startService(view: View) {
        //TODO:
        // make global boolean, it will be set default as false, becomes true here,
        // if startService is pressed when already being active it will be ignored
        setContentView(R.layout.activity_main)
        mMainService = MainService(this)
        mServiceIntent = Intent(this, MainService::class.java)
        if (!isMyServiceRunning(MainService::class.java)) { //MainService::class.java used to be mMainService.getClass() but that wasnt working
            startService(mServiceIntent)
        } else {
            toast("scan already running")
        }
        //val intent = Intent(this, MainService::class.java)
        //startService(intent)
        //used these 2 lines before checking if service was on or not
    }

    fun stopService(view: View) {
        //TODO:
        // make global boolean, it will be set default as false, becomes true here,
        // if startService is pressed when already being active it will be ignored
        val intent = Intent(this, MainService::class.java)
        stopService(intent)
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("isMyServiceRunning?", true.toString() + "")
                return true
            }
        }
        Log.i("isMyServiceRunning?", false.toString() + "")
        return false
    }

    fun showNotification(view: View) {
        // Builds a notification
        val notificBuilder = NotificationCompat.Builder(this)
                .setContentTitle(resources.getString(R.string.msg))
                .setContentText(resources.getString(R.string.newmsg))
                .setTicker(resources.getString(R.string.alnew))
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

    fun addcrypto(view: View) {
        startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to true)
    }

    fun addstock(view: View) {
        startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to false)
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

        Log.d("playAlarm", "playAlarm")
        // Define a time value of 5 seconds
        val alertTime = GregorianCalendar().timeInMillis + 5

        // Define our intention of executing AlertReceiver
        val alertIntent = Intent(this, AlertReceiver::class.java)
        alertIntent.putExtra("message1", "ayyyy")
        alertIntent.putExtra("message2", "ayyyy2")
        alertIntent.putExtra("message3", "ayyyy3")
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
                Log.d("beforeAlarm", "mangracina55")
                // val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                //val num : LongArray = longArrayOf(1000,1000,1000)
                //v.vibrate(num,3)
                //updat.cancel(true)
                //updat.pause(intent.getStringExtra("delay").toLong())
                //Log.d("slept","canceled " + intent.getStringExtra("delay"))

                //Thread.sleep(intent.getStringExtra("delay").toLong() * 6000)
                //Log.d("slept",intent.getStringExtra("delay"))
                //updat.execute("h")

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
}