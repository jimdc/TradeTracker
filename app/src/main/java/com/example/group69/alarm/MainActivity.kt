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
import android.arch.lifecycle.Observer
import android.content.ServiceConnection
import android.content.ComponentName
import android.os.IBinder
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


//import android.databinding.DataBindingUtil

/**
 * @param[isNotificActive] tracks if the notification is active on the taskbar.
 * @param[notificationManager] allows us to notify user that something happened in the backgorund.
 * @param[notifID] is used to track notifications
 */

lateinit var dss: DatabaseSortaService
var isSnooze: Boolean = false
var snoozeTime: Double = 0.0;
var scanRunning: Boolean = false

class MainActivity : AppCompatActivity() {

    private var mServiceIntent: Intent? = null
    private var mMainService: MainService? = null

    val servRunning = true

    internal lateinit var notificationManager: NotificationManager

    internal var notifID = 33
    internal var isNotificActive = false
    var resultReceiver = createPriceBroadcastReceiver()

    lateinit var fragment: RecyclerViewFragment
    private val adapter: RecyclingStockAdapter by lazy { fragment.mAdapter }
    var mDrawerLayout: DrawerLayout? = null

    /**
     * Registers broadcast receiver, populates stock listview.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dss = DatabaseSortaService(this.applicationContext)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            fragment = RecyclerViewFragment()
            transaction.replace(R.id.stock_content_fragment, fragment)
            transaction.commit()
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                resultReceiver, IntentFilter("com.example.group69.alarm"))

        val mSwipeRefreshLayout = findViewById(R.id.swiperefresh) as SwipeRefreshLayout
        mSwipeRefreshLayout?.setOnRefreshListener {
            Log.i("MainActivity", "onRefresh called. This does nothing though.")
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
                R.id.menu_snooze -> snooze()
                R.id.menu_add_stock -> startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to false)
                R.id.menu_add_crypto -> startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to true)
            }
            true
        }
    }

    private val mDisposable = CompositeDisposable()
    override fun onStart() {
        super.onStart()
        // Subscribe to the emissions of the stock from the view model.
        // Update the stock text view, at every onNext emission.
        // In case of error, log the exception.
        mDisposable.add(dss.getFlowableStocklist()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { Stocklist -> adapter?.refresh(Stocklist) },
                        { throwable -> Log.d("Disposable::fail", throwable.message)}
                )
        )
    }

    override fun onStop() {
        super.onStop()
        mDisposable.clear()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity_action_menu, menu)

        val mSwitchScanningOrNot = menu?.findItem(R.id.show_scanning)?.actionView?.findViewById(R.id.show_scanning_switch) as? ToggleButton
        mSwitchScanningOrNot?.isChecked = isMyServiceRunning(MainService::class.java)

        mSwitchScanningOrNot?.setOnCheckedChangeListener { button, boo -> when(boo) {
                true -> {
                    mMainService = MainService(this)
                    mServiceIntent = Intent(this, MainService::class.java)
                    if (!isMyServiceRunning(MainService::class.java)) {
                        startService(mServiceIntent)
                    } else {
                        toast("scan already running")
                    }
                    scanRunning = true
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
        when (item.itemId) {
            android.R.id.home -> mDrawerLayout?.openDrawer(GravityCompat.START)
            R.id.action_snooze -> snooze()
            R.id.action_add_stock -> startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to false)
            R.id.action_add_crypto -> startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to true)
        }

        return super.onOptionsItemSelected(item)
    }
    fun snooze() {
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
                if (!(iHour.text.toString().isEmpty() && iMinute.text.toString().isEmpty())) {
                    /* if(iHour.text.toString().isEmpty())
                        snoozeTime = iMinute.text.toString().toDouble() * 60
                    if(iMinute.text.toString().isEmpty())
                        snoozeTime = iHour.text.toString().toDouble() * 3600
                    if(!iHour.text.toString().isEmpty() && !iMinute.text.toString().isEmpty())
                        snoozeTime = iHour.text.toString().toDouble() * 3600 + iMinute.text.toString().toDouble() * 60 */

                    Log.d("main","scan pausing")
                    //var stockid = Calendar.getInstance().getTimeInMillis()
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
            toast("scan isn't running, can't snooze")
        }
    }

    /**
     * Queries Datenbank [NewestTableName] for stocks list and returns it
     * @return the rows of stocks, or an empty list if Datenbank fails
     * @seealso [Updaten.getStocklistFromDB]
     */
    @Synchronized
    fun getStocklistFromDB() : List<Stock> {
        return dss.getStocklistFromDB()
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

    final val BROADCAST_PRICE_UPDATE = "BROADCAST_PRICE_UPDATE"
    /**
     * @return BroadcastReceiver that logs when it is registered
     */
    private fun createPriceBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val rStockid = intent.getLongExtra("stockid", -666)
                val rPrice = intent.getDoubleExtra("currentprice", -666.0)
                val rTime = intent.getStringExtra("time") ?: "not found"
                when(intent.action) {
                    "com.example.group69.alarm" -> adapter?.setCurrentPrice(rStockid, rPrice, rTime)
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
        dss.cleanup()
        super.onDestroy()
    }
}