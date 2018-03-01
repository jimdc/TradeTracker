package com.example.group69.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
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
import android.app.ActivityManager
import android.content.ServiceConnection
import android.content.ComponentName
import android.os.IBinder
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.*

//import android.databinding.DataBindingUtil

/**
 * @param[isNotificActive] tracks if the notification is active on the taskbar.
 * @param[notificationManager] allows us to notify user that something happened in the backgorund.
 * @param[notifID] is used to track notifications
 */

lateinit var dbService: DatabaseService
var dbsBound: Boolean = false

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
    var mDrawerLayout: DrawerLayout? = null

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

    var SnoozeButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        LocalBroadcastManager.getInstance(this).registerReceiver(
                resultReceiver, IntentFilter("com.example.group69.alarm"))

        var intent = Intent(this, DatabaseService::class.java)
        if (!bindService(intent, dbConnection, Context.BIND_AUTO_CREATE))
            Log.e("MainActivity", "onCreate: not able to bind dbConnection")


        SnoozeButton = findViewById(R.id.timeDelayButton)
        listView = findViewById<ListView>(R.id.listView)
        adapter = UserListAdapter(this, stocksList)

        listView?.adapter = adapter
        listView?.setOnItemClickListener { parent, view, position, id ->
            alert("Are you sure you want to delete row " + position.toString(), "Confirm") {
                positiveButton("Yes") {
                    toast("Row " + position.toString() +
                            if (deletestock(position)==true) (" deleted.") else " not deleted.")
                }
                negativeButton("No") {  toast("OK, nothing was deleted.") }
            }.show()
        }

        val mSwipeRefreshLayout = findViewById(R.id.swiperefresh) as SwipeRefreshLayout
        mSwipeRefreshLayout?.setOnRefreshListener {
            Log.i("MainActivity", "onRefresh called from SwipeRefreshLayout")
            refreshULA()
            mSwipeRefreshLayout.setRefreshing(false)
        }

        val toolbar = findViewById(R.id.cooltoolbar) as? android.support.v7.widget.Toolbar
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        mDrawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener {
            it.setChecked(true)
            mDrawerLayout?.closeDrawers()

            when(it.itemId) {
                R.id.menu_snooze -> startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to true,"snooze" to true)
                R.id.menu_add_stock -> startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to true)
                R.id.menu_add_crypto -> startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to false)
            }

            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity_action_menu, menu)

        val mSwitchScanningOrNot = menu?.findItem(R.id.show_scanning)?.getActionView()?.findViewById(R.id.show_scanning_switch) as? ToggleButton

        mMainService = MainService(this)
        mServiceIntent = Intent(this, MainService::class.java)
        if (isMyServiceRunning(MainService::class.java)) {
            toast("Service already running, shutting it down.")
            val intent = Intent(this, MainService::class.java)
            stopService(intent)
        }

        mSwitchScanningOrNot?.setOnCheckedChangeListener { button, boo -> when(boo) {
                true -> {
                    mMainService = MainService(this)
                    mServiceIntent = Intent(this, MainService::class.java)
                    if (!isMyServiceRunning(MainService::class.java)) {
                        startService(mServiceIntent)
                    } else {
                        toast("scan already running")
                    }
                }
                false -> {
                    val intent = Intent(this, MainService::class.java)
                    stopService(intent)
                }
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * For navigation drawer in toolbar
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> mDrawerLayout?.openDrawer(GravityCompat.START)
            R.id.action_snooze -> startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to true,"snooze" to true)
            R.id.action_add_stock -> startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to true)
            R.id.action_add_crypto -> startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to false)
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Snooze button.
     */
    fun timeDelay(view: View) {
        startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to true,"snooze" to true)
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

    override fun onResume() {
        super.onResume()
        refreshULA()
    }

    /**
     * Updates [stocksList] from Datenbank then refreshes UI
     */
    fun refreshULA() {
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