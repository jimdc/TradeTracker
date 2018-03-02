package com.example.group69.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.util.Log
import org.jetbrains.anko.*
import android.content.BroadcastReceiver
import android.support.v4.content.LocalBroadcastManager
import android.content.IntentFilter
import android.widget.ListView
import android.content.ServiceConnection
import android.content.ComponentName
import android.os.IBinder
import android.widget.Button
import android.widget.Toast
import android.widget.EditText
import java.util.*


//import android.databinding.DataBindingUtil

/**
 * @param[isNotificActive] tracks if the notification is active on the taskbar.
 * @param[notificationManager] allows us to notify user that something happened in the backgorund.
 * @param[notifID] is used to track notifications
 */

lateinit var dbService: DatabaseService
var dbsBound: Boolean = false
var isSnooze: Boolean = false
var snoozeTime: Double = 0.0;
var scanRunning: Boolean = false

class MainActivity : AppCompatActivity() {

    private var mServiceIntent: Intent? = null
    private var mMainService: MainService? = null
    val servRunning = true
    var stocksList: List<Stock> = ArrayList()
    internal lateinit var notificationManager: NotificationManager

    internal var notifID = 33
    internal var isNotificActive = false
    var resultReceiver = createPriceBroadcastReceiver()

    var listView: ListView? = null
    var adapter: UserListAdapter? = null

    var dbConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as DatabaseService.LocalBinder
            dbService = binder.service
            dbsBound = true
            Log.d("MainActivity", "dbService connected")
            stocksList = dbService.getStocklistFromDB()
            adapter?.refresh(stocksList)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            dbsBound = false
        }
    }

    /**
     * Registers broadcast receiver, populates stock listview.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        LocalBroadcastManager.getInstance(this).registerReceiver(
                resultReceiver, IntentFilter("com.example.group69.alarm"))

        var intent = Intent(this, DatabaseService::class.java)
        if (!bindService(intent, dbConnection, Context.BIND_AUTO_CREATE))
            Log.e("MainActivity", "onCreate: not able to bind dbConnection")

        if (isMyServiceRunning(MainService::class.java)) {
            scanRunning = true
            toast("scan still running")
        }
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
    fun snooze(view: View) {
        Log.d("snoozeee","snoozin")
        if (scanRunning) {
            val mBuilder = AlertDialog.Builder(this@MainActivity)
            val mView = layoutInflater.inflate(R.layout.snooze_dialogue, null)
            val iHour = mView.findViewById(R.id.inputHour) as EditText
            val iMinute = mView.findViewById(R.id.inputMinute) as EditText
            val mLogin = mView.findViewById(R.id.btnSnooze) as Button

            mBuilder.setView(mView)
            val dialog = mBuilder.create()
            dialog.show()
            mLogin.setOnClickListener(View.OnClickListener {
                if (!dbsBound) {
                    toast("Scan isn't even running dude")
                    Log.d("ayyy111","ayyy111")
                }
                if (!(iHour.text.toString().isEmpty() && iMinute.text.toString().isEmpty())) {
                    /* if(iHour.text.toString().isEmpty())
                        snoozeTime = iMinute.text.toString().toDouble() * 60
                    if(iMinute.text.toString().isEmpty())
                        snoozeTime = iHour.text.toString().toDouble() * 3600
                    if(!iHour.text.toString().isEmpty() && !iMinute.text.toString().isEmpty())
                        snoozeTime = iHour.text.toString().toDouble() * 3600 + iMinute.text.toString().toDouble() * 60 */

                    Log.d("main","scan pausing")
                    var stockid = Calendar.getInstance().getTimeInMillis()
                    snoozeTime = iHour.text.toString().toDouble() * 3600 + iMinute.text.toString().toDouble() * 60
                    isSnooze = true
                    val seconds = iHour.text.toString().toDouble() * 3600 + iMinute.text.toString().toDouble() * 60
                  /*  val snoozeEntry = Stock(stockid, "snoozee", seconds
                            ?: 6.66, false, false, false)
                    if (dbsBound) {
                        dbService.addeditstock(snoozeEntry)
                    } else {
                        Log.e("AddButton", "OnClickListener: dbsBound = false, so did nothing.")
                    } */
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@MainActivity,
                            getString(R.string.invalid_entry),
                            Toast.LENGTH_SHORT).show()
                }
            })

        }
        else {
            toast("scan isn't even running, can't snooze")
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
        if (dbsBound) {
            return dbService.deletestockInternal(target.stockid)
        }
        Log.e("MainActivity", "deletestock: dbsBound = false, so did nothing.")
        return false
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
        if (dbsBound) {
            return dbService.getStocklistFromDB()
        }
        Log.e("MainActivity", "getStocklistFromDB: dbsBound = false, so did nothing.")
        return emptyList()
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
        scanRunning = true
    }

    /**
     * Stop the ScannerService
     * @param[view] The main activity view
     * @todo Make global boolean to signal if [startService] already pressed
     */
    fun stopService(view: View) {
        val intent = Intent(this, MainService::class.java)
        stopService(intent)
        scanRunning = false
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

    final val BROADCAST_PRICE_UPDATE = "BROADCAST_PRICE_UPDATE"
    /**
     * @return BroadcastReceiver that logs when it is registered
     */
    private fun createPriceBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val rStockid = intent.getLongExtra("stockid", -666)
                val rPrice = intent.getDoubleExtra("currentprice", -666.0)
                when(intent?.action) {
                    "com.example.group69.alarm" -> adapter?.setCurrentPrice(rStockid, rPrice)
                }
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
        unbindService(dbConnection)
        dbsBound = false
        super.onDestroy()
    }
}