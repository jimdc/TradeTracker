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
import android.widget.ListView;

import java.util.GregorianCalendar
import android.content.BroadcastReceiver
import android.support.v4.content.LocalBroadcastManager
import android.content.IntentFilter
import android.app.ActivityManager
import kotlinx.android.synthetic.main.activity_main.*

/* TODOyu
    Name for app? Trademaster?
 * BUGS: (this is mainly in updaten) if app is killed while alarm vibration is going off, the stock will not be
 * removed from database and will be removed as soon as the service restarts (service auto-restarts even if app is killed)
 * -- if alarm vibration is going off and stop scan is pressed, the same problem happens. once scan is started again you
 * will get notified again for the same stock. as long as scan isn't stopped or app isn't killed mid-vibration this should
 * not be an issue and is honestly not worth fixing until the app is published and I feel it's worth taking the time to
 * fix. One extra notification is not gonna kill anyone or ruin the app
 *
 * FEATURES TO ADD:
 * ---the main screen should be 2 lists vertically stacked, the top being the stocks and the bottom being the cryptos.
 * there should be a a thin bar separating them which can be dragged up/down to adjust view to see more or all of a preferred list.
 * ***this setting should be saved as a state so even if app is restarted the setting that was left will be what is shown
 * **this is important because someone who only trades in cryptos will not want to have to adjust everytime they open the app so that
 * they only see the crypto list. --
 *
 * --when a stock was removed from the momentum check list, perhaps the user wanted to detect for +10% price in 2 hour timespan, by
 *       intervals of 20 seconds. suppose it rose 10% in 20 seconds, then immidiatly dropped, so the notification goes off but the
 *       user sees they missed the chance and it was a quick change. there can be a list 'recently deleted' which it can be found so
 *       that the user can re-add the sto67ck rapidly (not such an important feature)
 *    a better way to prevent this is to make the interval say 1 minute, so that less battery power is used as well as small giant
 *    changes which immidiatly fall back down.
 *
 *
 * ---There should be 2 buttons for adding. 1 for add crypto, one for add stock. orginally i wanted to have a generic 'add to list'
 * which could have 2 bubbles to fill to specify whether something is a crypto or stock. but even if it is at top, someone might
 * forget to change from one to the other (suppose someone who deals with both) and quickly adds a target. If they want to add ethereum
 * but leave stock checked, it will work fine because the ticker for ethereum is eth, and the is also a stock with the same ticker.
 * this could leave someone potentially loosing thousands of dollars. to make sure this never happens to myself or anyone else, 2 add
 * buttons are needed. this also makes one less click needed to quickly add a stock/crypto to the list.
 *
 * --volume change alerts. if volume increases to X amount, notify me. if volume increases by X% in Y time frame notify me. if price decreases
 * by at least 10% in at least a one week period, followed by 2 minute period rises 3% OR a volume increase of 60%, notify me
 * if volume increases by 100% in 20 minute period or less, within the times of 10:30am and 4pm, notify me
 * If volume increases by 1,000,000 in a 20 minute period, notify me. accuracy by 20 seconds) == minimal timespan of 20 seconds means every 20 seconds
 * we check all the 60 datapoints to see if there was a 20
 *
 *
 * simpler and easier on battery power would be: check every 30 seconds if volume has reached 2,500,000. 1 check every 30 seconds.
 *
 * --price movement alerts
 * if price increases by at least 3% in a 30 second period. the 20 minute period, checking 20 second intervals. this will
 * 2 week period checking 3 minute intervals (20*24) = 480 data points per day * 14 = ~6720 datapoint checks per every 3 minute
 * period (not bad), if the price percent between any of the is in the specified range user is notified.
 *
 * --- 1 week period: 1 minute intervals, 3% price increase: every minute, there will be 10,080 comparisons. if phone is on for 12 hours,
 * that is 7.2 million comparison checks, which is not very bad. this max number of comparisons will only be reached if this has 1 week of data.
 * gaps in the data, say having phone off for 1 hour after having 1 week of data, will take 1 hour to refill back to the max table size.
 *
 * this will require a table which starts off as just 1 row, with 2 columns, timestamp(primary key for this table) and the price.
 * every minute a new entry is added to the database in the beggining, and we check every value in the database to see if the percent
 * change has been met. suppose we want
 *
 * suppose after being run for 1 week, we turn the scan off for 1 day, (use same 1 week scan as last example), the oldest value will now be
 * 8 days old, so comparing might have a 3% change, while it is possible that there was a 3% change in 1 week during the 1 day of no datapoints,
 * it is also possible that the 3% change is only from the 8 day period. the idea is too keep the scan on, if it is not on anything missed is because
 * the scan was not being run which it always should be for serious traders not wanting to miss out and have the most accuracy from this app.
 *  to fix this:
 *    **** when scan starts: any timestamp older than one week will result in all of those rows being deleted.
 *
 *j
 *
 * for 1 hour period with 15 seconds intervals, there will be a new data point every 20 with the oldest datapoint pushed out the queue.
 * 240 checks will occur every 15 seconds (~1000 per minute, 60000 per hour, 1.4 million per 24 hours) this is not bad especially compared
 * to checking every minute for a week which after a week will have 14 million checks per minute
 *
 *
 *
 */

///NotificationManager : Allows us to notify the user that something happened in the background
// AlarmManager : Allows you to schedule for your application to do something at a later date
// even if it is in the background

class MainActivity : AppCompatActivity() {

    var mServiceIntent: Intent? = null
    private var mMainService: MainService? = null
    val servRunning = true
    //val showNotificationBut: Button by lazy {  findViewById(R.id.showNotificationBut) as Button }
    val addstockButton: Button by lazy { findViewById(R.id.addstockButton) as Button }
    val addcryptoButton: Button by lazy { findViewById(R.id.addcryptoButton) as Button }
    val showstockButton: Button by lazy { findViewById(R.id.showstockButton) as Button }
    val timeDelayButton: Button by lazy { findViewById(R.id.timeDelayButton) as Button }
    lateinit var phraseListView: ListView
    lateinit var stopNotificationBut: Button
    lateinit var alertButton: Button
    var stocksTargets: List<Stock> = ArrayList()
    var stocksCurrent: List<Stock> = ArrayList()
    // Allows us to notify the user that something happened in the background
    internal lateinit var notificationManager: NotificationManager

    // Used to track notifications
    internal var notifID = 33
    //val updat = Updaten(this)

    // Used to track if notification is active in the task bar
    internal var isNotificActive = false
    var resultReceiver = createBroadcastReceiver()
    override fun onCreate(savedInstanceState: Bundle?) { //Only called when you restore from a saved state!
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        phraseListView = findViewById(R.id.phrase_ListView) as ListView

        Utility.setListViewHeightBasedOnChildren(phrase_ListView);

        // Initialize buttons
//        stopNotificationBut = findViewById(R.id.stopNotificationBut) as Button
        //alertButton = findViewById(R.id.alertButton) as Button


        LocalBroadcastManager.getInstance(this).registerReceiver(resultReceiver, IntentFilter("com.example.group69.alarm"))
        Log.d("dictionary", "")
        runOnUiThread {
            try {
                database.use {
                    val sresult = select("TableView2", "_stockid", "ticker", "target", "ab", "phone", "crypto")

                    sresult.exec() {
                        if (count > 0) {
                            val parser = rowParser { stockid: Long, ticker: String, target: Double, above: Long, phone: Long, crypto: Long ->
                                Stock(stockid, ticker, target, above, phone, crypto)
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


            //updat.execute("hi")
            //to use this to cancel if needed
            //updat.cancel(true)

            //activates doInBackground
        }
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
        startActivity<HotStockActivity>()
    }

    fun addcrypto(view: View) {
        startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to true)
    }

    fun addstock(view: View) {
        startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to false)
    }

    fun showstocks(view: View) {
        var stocklist: List<Stock> = ArrayList()
        try {
            database.use {
                val sresult = select("TableView2", "_stockid", "ticker", "target", "ab", "phone", "crypto")
                sresult.exec() {
                    if (count > 0) {
                        val parser = rowParser { stockid: Long, ticker: String, target: Double, above: Long, phone: Long, crypto: Long ->
                            Stock(stockid, ticker.toUpperCase(), target, above, phone, crypto)
                        }

                        stocklist = parseList(parser)
                    }
                }
            }
        } catch (e: android.database.sqlite.SQLiteException) {
            toast(e.toString())
            return
        }

        if (stocklist.isEmpty()) {
            toast(getResources().getString(R.string.failempty))
            return
        }

        var stocknamelist: List<CharSequence> = ArrayList()
        stocklist.forEach { i ->
            stocknamelist += i.toString()
        }

        selector(getResources().getString(R.string.choose1), stocknamelist) { i ->
            run {
                val st0ck = stocklist.get(i)
                startActivity<AddEditStockActivity>("EditingExisting" to true,
                        "EditingCrypto" to st0ck.crypto, "TheStock" to st0ck)
            }
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

        Log.d("playAlarm", "playAlarm")
        // Define a time value of 5 seconds
        val alertTime = GregorianCalendar().timeInMillis + 5

        // Define our intention of executing AlertReceiver
        val alertIntent = Intent(this, AlertReceiver::class.java)
        val alert1 = "ayyyy"
        val alert2 = "ayyyy2"
        val alert3 = "ayyyy3"
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

    fun deleteStockOfThisIndex(s: String) {

    }
}