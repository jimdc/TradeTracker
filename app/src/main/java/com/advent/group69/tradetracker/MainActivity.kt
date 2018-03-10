package com.advent.group69.tradetracker

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
import io.reactivex.schedulers.Timed
import android.content.SharedPreferences
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeConstants.SECONDS





/**
 * @param[isNotificActive] tracks if the notification is active on the taskbar.
 * @param[notificationManager] allows us to notify user that something happened in the backgorund.
 * @param[notifID] is used to track notifications
 */

lateinit var dbFunctions: WrapperAroundDao

var isSnoozing: Boolean = false
var snoozeMsecTotal: Long = 0
var snoozeMsecElapsed: Long = 0
var snoozeMsecInterval: Long = 1000

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

        dbFunctions = WrapperAroundDao(this.applicationContext)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            fragment = RecyclerViewFragment()
            transaction.replace(R.id.stock_content_fragment, fragment)
            transaction.commit()
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                currentPriceReceiver, IntentFilter("com.advent.group69.tradetracker"))

        val toolbar = findViewById(R.id.cooltoolbar) as? android.support.v7.widget.Toolbar
        infoSnoozer = findViewById(R.id.infoSnoozing)
        progressSnoozer = findViewById(R.id.snoozeProgressBar)
        setSupportActionBar(toolbar)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val gridlayoutPref = sharedPref.getBoolean(SettingsActivity.KEYS.gridlayout(), false)
    }

    private lateinit var infoSnoozer: TextView
    private lateinit var progressSnoozer: ProgressBar

    private val mDisposable = CompositeDisposable()
    override fun onStart() {
        super.onStart()
        // Subscribe to stock emissions from the database
        // On every onNext emission update textview or log exception
        mDisposable.add(dbFunctions.getFlowableStocklist()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { Stocklist -> adapter.refresh(Stocklist) },
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
                    try {
                        val hours = if (iHourt.isEmpty()) 0L else iHourt.toLong()
                        val minutes = if (iMinutet.isEmpty()) 0L else iMinutet.toLong()
                        snoozeMsecTotal = 1000*(hours*3600 + minutes*60)
                        snoozeMsecElapsed = 0L
                    } catch (nfe: NumberFormatException) {
                        Toast.makeText(this@MainActivity, getString(R.string.NaN, iHourt + "/" + iMinutet), Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }

                    var IterationsWhenDone = snoozeMsecTotal / snoozeMsecInterval
                    val timerDisposable = Observable.interval(snoozeMsecInterval, TimeUnit.MILLISECONDS, Schedulers.io())
                            .take(IterationsWhenDone)
                            .map({ v -> IterationsWhenDone - v })
                            .subscribe(
                                    { onNext ->
                                        snoozeMsecElapsed += snoozeMsecInterval
                                        this.applicationContext.onUiThread {
                                            progressSnoozer.setProgress(snoozeMsecElapsed.toInt())
                                            infoSnoozer.text = resources.getString(R.string.snoozedfor, snoozeMsecElapsed, snoozeMsecTotal)
                                        }
                                    },
                                    { onError ->
                                        this.applicationContext.onUiThread {
                                            toast("Something went wrong with the snoozer timer.")
                                        }
                                    },
                                    {
                                        isSnoozing = false //done
                                        this.applicationContext.onUiThread {
                                            infoSnoozer.text = resources.getString(R.string.notsnoozing)
                                        }
                                    },
                                    { onSubscribe ->
                                        isSnoozing = true //start
                                        this.applicationContext.onUiThread {
                                            progressSnoozer.max = snoozeMsecTotal.toInt()
                                            infoSnoozer.text = resources.getString(R.string.snoozingfor, snoozeMsecTotal)
                                        }
                                    })
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

    val BROADCAST_PRICE_UPDATE = "BROADCAST_PRICE_UPDATE"
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
                    "com.advent.group69.tradetracker" -> adapter?.setCurrentPrice(rStockid, rPrice, rTime)
                }
            }
        }
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