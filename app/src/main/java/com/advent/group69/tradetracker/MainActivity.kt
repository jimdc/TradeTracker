package com.advent.group69.tradetracker

import android.annotation.SuppressLint
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
import android.support.v7.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import android.os.BatteryManager
import android.os.Build
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import io.fabric.sdk.android.Fabric



lateinit var dbFunctions: WrapperAroundDao

//these can be used in other kotlin files
var powerSavingOn = true
var isSnoozing: Boolean = false

var wakeupSystemTime: Long = 0
var snoozeTimeRemaining: Long = 0
var snoozeMsecInterval: Long = 1000

var notifiedOfPowerSaving: Boolean = false
var notif1: Boolean = false

/**
 * @param[isNotificActive] tracks if the notification is active on the taskbar.
 * @param[notificationManager] allows us to notify user that something happened in the backgorund.
 * @param[notifID] is used to track notifications
 */
class MainActivity : AppCompatActivity() {

    private var mServiceIntent: Intent? = null
    internal lateinit var notificationManager: NotificationManager

    internal var notifID = 33
    internal var isNotificActive = false
    var currentPriceReceiver = createPriceBroadcastReceiver()

    lateinit var fragment: RecyclerViewFragment
    private val adapter: RecyclingStockAdapter by lazy { fragment.mAdapter }

    /**
     * Registers broadcast receiver, populates stock listview.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        Fabric.with(this, Answers())

        dbFunctions = WrapperAroundDao(this.applicationContext)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            fragment = RecyclerViewFragment()
            transaction.replace(R.id.stock_content_fragment, fragment)
            transaction.commit()
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                currentPriceReceiver, IntentFilter("PRICEUPDATE"))

        val toolbar = findViewById(R.id.cooltoolbar) as? android.support.v7.widget.Toolbar
        infoSnoozer = findViewById(R.id.infoSnoozing)
        progressSnoozer = findViewById(R.id.snoozeProgressBar)
        setSupportActionBar(toolbar)
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && powerManager.isPowerSaveMode) {
            powerSavingOn = true
        }
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }

    private lateinit var infoSnoozer: TextView
    private lateinit var progressSnoozer: ProgressBar

    fun forceCrash(view: View) {
        throw RuntimeException("This is a crash")
    }


    private val mDisposable = CompositeDisposable()
    override fun onStart() {
        super.onStart()
        // Subscribe to stock emissions from the database
        // On every onNext emission update textview or log exception

        mDisposable.add(dbFunctions.getFlowableStocklist()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { Stocklist -> adapter.refresh(Stocklist)},
                        { throwable -> Log.d("Disposable::fail", throwable.message)}
                )
        )
    }

    override fun onStop() {
        super.onStop()
        mDisposable.clear() //Unsubscribe from database updates of stocklist
    }

    /**
     * This menu is the "add stock, add crypto, openSnoozeDialog, settings" on top.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity_action_menu, menu)

        val mSwitchScanningOrNot = menu?.findItem(R.id.show_scanning)?.actionView?.findViewById(R.id.show_scanning_switch) as? ToggleButton
        mSwitchScanningOrNot?.isChecked = isMyServiceRunning(MainService::class.java)
        mServiceIntent = Intent(this, MainService::class.java)

        mSwitchScanningOrNot?.setOnCheckedChangeListener { button, boo -> when(boo) {
                true -> {
                    if (!isMyServiceRunning(MainService::class.java))
                        startService(mServiceIntent)
                    else
                        toast("Scan already running")
                }
                false -> { stopService(mServiceIntent) }
            }
        }

        return super.onCreateOptionsMenu(menu)

    }

    /**
    * For navigation drawer in toolbar
    */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> startActivity<SettingsActivity>()
            R.id.action_snooze -> openSnoozeDialog()
            R.id.action_add_stock -> startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to false)
            R.id.action_add_crypto -> startActivity<AddEditStockActivity>("EditingExisting" to false, "EditingCrypto" to true)
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("InflateParams")
    private fun openSnoozeDialog() {
        if (isSnoozing) { toast(R.string.alreadysnoozing); return }
        Log.i("MainActivity","Opening snooze dialog")
        if (isMyServiceRunning(MainService::class.java)) {
            val mBuilder = AlertDialog.Builder(this@MainActivity)
            val mView = layoutInflater.inflate(R.layout.snooze_dialog, null)
            val iHour = mView.findViewById(R.id.inputHour) as EditText
            val iMinute = mView.findViewById(R.id.inputMinute) as EditText
            val bConfirmSnooze = mView.findViewById(R.id.btnSnooze) as Button

            mBuilder.setView(mView)
            val dialog = mBuilder.create()
            dialog.show()
            bConfirmSnooze.setOnClickListener {
                val iHourt = iHour.text.trim().toString()
                val iMinutet = iMinute.text.trim().toString()

                if (iHourt.isEmpty() && iMinutet.isEmpty())
                    Toast.makeText(this@MainActivity,
                            getString(R.string.invalid_entry), Toast.LENGTH_SHORT).show()
                else {
                    var snoozeMsecTotal: Long = 0
                    try {
                        val hours = if (iHourt.isEmpty()) 0L else iHourt.toLong()
                        val minutes = if (iMinutet.isEmpty()) 0L else iMinutet.toLong()
                        snoozeMsecTotal = 1000*(hours*3600 + minutes*60)
                        snoozeTimeRemaining = snoozeMsecTotal
                        wakeupSystemTime = System.currentTimeMillis()+snoozeMsecTotal

                    } catch (nfe: NumberFormatException) {
                        Toast.makeText(this@MainActivity, getString(R.string.NaN, iHourt + "/" + iMinutet), Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        finish()
                    }

                    isSnoozing = true
                    infoSnoozer.text = resources.getString(R.string.snoozingfor, snoozeMsecTotal)
                    Log.i("MainActivity", "isSnoozing set to true. Scan pausing.")
                    async {
                        progressSnoozer.max = snoozeMsecTotal.toInt()
                        while(isSnoozing) {
                            uiThread {
                                progressSnoozer.setProgress(snoozeTimeRemaining.toInt())
                                infoSnoozer.text = resources.getString(R.string.snoozeremain, snoozeTimeRemaining, snoozeMsecTotal)
                            }
                            Utility.TryToSleepFor(snoozeMsecInterval)
                        }

                        uiThread {
                            infoSnoozer.text = resources.getString(R.string.notsnoozing)
                        }
                    }

                    /**
                     * RX version of timer. It did not persist through activity changes so not used for now.
                     */
                    /*val iterationsWhenDone = snoozeMsecTotal / snoozeMsecInterval
                    val timerDisposable = Observable.interval(snoozeMsecInterval, TimeUnit.MILLISECONDS, Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()) //To show things on UI
                            .take(iterationsWhenDone)
                            .map({ v -> iterationsWhenDone - v })
                            .subscribe(
                                    { onNext ->
                                        snoozeTimeRemaining += snoozeMsecInterval
                                        progressSnoozer.setProgress(snoozeTimeRemaining.toInt())
                                        infoSnoozer.text = resources.getString(R.string.snoozedfor, snoozeTimeRemaining, snoozeMsecTotal)
                                    },
                                    { onError ->
                                        toast("Something went wrong with the snoozer timer.")
                                    },
                                    {
                                        isSnoozing = false //done
                                        infoSnoozer.text = resources.getString(R.string.notsnoozing)
                                    },
                                    { onSubscribe ->
                                        isSnoozing = true //start
                                        progressSnoozer.max = snoozeMsecTotal.toInt()
                                        infoSnoozer.text = resources.getString(R.string.snoozingfor, snoozeMsecTotal)
                                    })
                    */
                    dialog.dismiss()
                }
            }

        } else {
            toast(R.string.onlysnoozewhenscanning)
        }
    }

    /**
     * Query the system for if a service is already running.
     * @param[serviceClass] the service class
     * @return true if running, false if not
     * @sample startService
     */
    @Suppress("DEPRECATION")
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

    fun showNotification() {
        val notificBuilder = NotificationCompat.Builder(this)
                .setContentTitle("please disable power saving mode to keep scanning while phone screen is off")
                .setContentText("CLICK THIS NOTIFICATION for more information")
                .setTicker("C")
                .setSmallIcon(R.drawable.stocklogo)

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
     * @return BroadcastReceiver that logs when it is registered
     */

    private fun createPriceBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val rStockid = intent.getLongExtra("stockid", -666)
                val rPrice = intent.getDoubleExtra("currentprice", -666.0)
                val rTime = intent.getStringExtra("time") ?: "not found"
                Log.v("MainActivity", "Received price update of $rStockid as $rPrice")

                if(rStockid == 1111111111111111111 && powerSavingOn) {
                    if (notif1) {
                        showNotification()
                        toast("Turn off power saving mode so scan can run while phone is sleeping")
                        notifiedOfPowerSaving = true
                    } else {
                        notif1 = true
                    }

                    return
                }
                when (intent.action) {
                    "PRICEUPDATE" -> adapter.setCurrentPrice(rStockid, rPrice, rTime)
                }
            }
        }
    }

    fun isPhonePluggedIn(context: Context): Boolean {
        var charging = false

        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryIntent!!.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val batteryCharge = status == BatteryManager.BATTERY_STATUS_CHARGING

        val chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
        val acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC

        if (batteryCharge) charging = true
        if (usbCharge) charging = true
        if (acCharge) charging = true

        return charging
    }

    /**
     * Unregister the result receiver
     */
    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(currentPriceReceiver)
        dbFunctions.cleanup() //Close database access
        super.onDestroy()
    }
}