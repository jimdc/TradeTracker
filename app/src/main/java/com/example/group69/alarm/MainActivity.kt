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

/**
 * @param[isNotificActive] tracks if the notification is active on the taskbar.
 * @param[notificationManager] allows us to notify user that something happened in the backgorund.
 * @param[notifID] is used to track notifications
 */

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
                        toast("Row " + position.toString() + if (deleted==true) (" deleted.") else "not deleted.")
                    }
                    negativeButton("No") {  toast("OK, nothing was deleted.") }
                }.show()
            }
        }
    }

    /**
     * Deletes the stock from database, from [stocksList], and refreshes UI.
     * @param[position] the index of the stock in [stocksList]; same as in UI
     * @return true if SQLite helper could find the stock to delete
     * @sample onCreate
     */

    fun deletestock(position: Int) : Boolean {
        val target = stocksList.get(position) as Stock
        return deletestockInternal(target.stockid);
    }

    private fun deletestockInternal(stockid: Long) : Boolean {
        var rez = 0

        database.use {
            rez = delete(NewestTableName, "_stockid = ?", arrayOf(stockid.toString()))
        }

        if (rez > 0) {
            stocksList = getStocklistFromDB()
            adapter?.refresh(stocksList)
            return true
        }

        return false
    }

    /**
     * Updates [stocksList] from database then refreshes UI
     */
    override fun onResume() {
        super.onResume()
        stocksList = getStocklistFromDB()
        adapter?.refresh(stocksList)
    }

    /**
     * Queries database [NewestTableName] for stocks list and returns it
     * @return the rows of stocks, or an empty list if database fails
     * @seealso [Updaten.getStocklistFromDB], it should be identical
     */
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
    fun addcrypto(view: View) {
        startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to true)
    }

    /**
     * Launch [AddEditStockActivity] with empty, noncrypto assumptions
     * @param[view] required for onClick
     */
    fun addstock(view: View) {
        startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to false)
    }

    /**
     * @return BroadcastReceiver that logs when it is registered
     * @todo delete the stock after the alarm is set.
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