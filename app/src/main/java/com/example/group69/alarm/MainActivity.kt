package com.example.group69.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import org.jetbrains.anko.db.*
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.database.sqlite.SQLiteException
import android.view.View
import android.util.Log
import org.jetbrains.anko.*
import android.content.BroadcastReceiver
import android.support.v4.content.LocalBroadcastManager
import android.content.IntentFilter
import android.widget.ListView
import android.app.ActivityManager
import android.database.sqlite.SQLiteDatabase

//import android.databinding.DataBindingUtil

/**
 * @param[isNotificActive] tracks if the notification is active on the taskbar.
 * @param[notificationManager] allows us to notify user that something happened in the backgorund.
 * @param[notifID] is used to track notifications
 */

lateinit var manager: ManagedSQLiteOpenHelper
lateinit var Datenbank: SQLiteDatabase

class MainActivity : AppCompatActivity() {

    private var mServiceIntent: Intent? = null
    private var mMainService: MainService? = null
    val servRunning = true
    var stocksList: List<Stock> = ArrayList()
    internal lateinit var notificationManager: NotificationManager

    internal var notifID = 33
    internal var isNotificActive = false
    var resultReceiver = createBroadcastReceiver()

    var listView: ListView? = null
    var adapter: UserListAdapter? = null

    /**
     * Registers broadcast receiver, populates stock listview.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        manager = SQLiteSingleton.getInstance(getApplicationContext())
        com.example.group69.alarm.Datenbank = manager.writableDatabase

        setContentView(R.layout.activity_main)
        LocalBroadcastManager.getInstance(this).registerReceiver(
                resultReceiver, IntentFilter("com.example.group69.alarm"))

        //stocksList = getStocklistFromDB()
        //Log.v("MainActivity", if (stocksList.isEmpty()) "stocksList is now empty." else
        //    "stocksTargets: " + stocksList.map { it.ticker }.joinToString(", "))

        listView = findViewById<ListView>(R.id.listView)
        adapter = UserListAdapter(this, stocksList)

        listView?.adapter = adapter
        adapter?.notifyDataSetChanged()

        listView?.setOnItemClickListener { parent, view, position, id ->
            alert("Are you sure you want to delete row " + position.toString(), "Confirm") {
                positiveButton("Yes") {
                    toast("Row " + position.toString() +
                            if (deletestock(position)==true) (" deleted.") else " not deleted.")
                }
                negativeButton("No") {  toast("OK, nothing was deleted.") }
            }.show()
        }
    }

    /**
     * Deletes the stock from Datenbank, from [stocksList], and refreshes UI.
     * @param[position] the index of the stock in [stocksList]; same as in UI
     * @return true if SQLite helper could find the stock to delete
     * @sample onCreate
     * @todo just pass the stockid to the Updaten thread so it can delete it
     */

    fun deletestock(position: Int) : Boolean {
        val target = stocksList.get(position) as Stock
        return deletestockInternal(target.stockid)
    }

    @Synchronized
    private fun deletestockInternal(stockid: Long) : Boolean {
        var rez = 0

        try {
            Datenbank.use {
                var rez = Datenbank.delete(NewestTableName, "_stockid=$stockid")

                if (rez > 0) {
                    stocksList = getStocklistFromDB()
                    adapter?.refresh(stocksList)
                }
            }
        } catch (e: SQLiteException) {
            Log.e("MainActivity", "could not delete $stockid: " + e.toString())
        }

        return (rez > 0)
    }

    /**
     * Updates [stocksList] from Datenbank then refreshes UI
     */
    override fun onResume() {
        super.onResume()
        stocksList = getStocklistFromDB()
        adapter?.refresh(stocksList)
    }

    /**
     * Queries Datenbank [NewestTableName] for stocks list and returns it
     * @return the rows of stocks, or an empty list if Datenbank fails
     * @seealso [Updaten.getStocklistFromDB]
     */
    @Synchronized
    fun getStocklistFromDB() : List<Stock> {
        var results: List<Stock> = ArrayList()
        try {
            Datenbank.use {
                val sresult = Datenbank.select(NewestTableName, "_stockid", "ticker", "target", "ab", "phone", "crypto")

                sresult?.exec {
                    if (this.count > 0) {
                        val parser = rowParser { stockid: Long, ticker: String, target: Double, above: Long, phone: Long, crypto: Long ->
                            Stock(stockid, ticker, target, above, phone, crypto)
                        }
                        results = parseList(parser)
                    }
                }
            }
        } catch (e: SQLiteException) {
            Log.e("err gSlFDB: ", e.toString())
        }

        return results
    }

    /**
     * Start the ScannerService or notify that it's already running
     * @param[view] The main activity view
     * @todo Make global boolean to signal if [startService] already pressed
     */
    fun startService(view: View) {
        mMainService = MainService(this)
        mServiceIntent = Intent(this, MainService::class.java)
        if (!isMyServiceRunning(MainService::class.java)) {
            startService(mServiceIntent)
        } else {
            toast("scan already running")
        }
    }

    /**
     * Stop the ScannerService
     * @param[view] The main activity view
     * @todo Make global boolean to signal if [startService] already pressed
     */
    fun stopService(view: View) {
        val intent = Intent(this, MainService::class.java)
        stopService(intent)
    }

    /**
     * Query the system for if a service is already running.
     * @param[serviceClass] the service class
     * @return true if running, false if not
     * @sample startService
     */
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

    /**
     * Adds our notification intent to the stack
     * FLAG_UPDATE_CURRENT: keep but update intent
     * Obtains NotificationManger and posts it
     * [isNotificActive] ensures us we can't double stop
     * @param[view] current view from which to notify
     */
    fun showNotification(view: View) {
        val notificBuilder = NotificationCompat.Builder(this)
                .setContentTitle(resources.getString(R.string.msg))
                .setContentText(resources.getString(R.string.newmsg))
                .setTicker(resources.getString(R.string.alnew))
                .setSmallIcon(R.drawable.ntt_logo_24_24)

        val moreInfoIntent = Intent(this, MoreInfoNotification::class.java)
        val tStackBuilder = TaskStackBuilder.create(this)

        tStackBuilder.addParentStack(MoreInfoNotification::class.java)
        tStackBuilder.addNextIntent(moreInfoIntent)

        val pendingIntent = tStackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT)
        notificBuilder.setContentIntent(pendingIntent)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notifID, notificBuilder.build())

        isNotificActive = true
    }

    /**
     * Close the notification if it is still active.
     * @param[view] required for onClick
     */
    fun stopNotification(view: View) {
        if (isNotificActive) {
            notificationManager.cancel(notifID)
        }
    }

    /**
     * Launch [AddEditStockActivity] with empty, crypto assumptions
     * @param[view] required for onClick
     */
    fun LaunchAddCryptoActivity(view: View) {
        startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to true)
    }

    /**
     * Launch [AddEditStockActivity] with empty, noncrypto assumptions
     * @param[view] required for onClick
     */
    fun LaunchAddStockActivity(view: View) {
        startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to false)
    }

    /**
     * @return BroadcastReceiver that logs when it is registered
     */
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

    /**
     * Unregister the result receiver
     */
    override fun onDestroy() {
        if (resultReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(resultReceiver)
        }
        super.onDestroy()
    }
}