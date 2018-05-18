package com.advent.tradetracker.viewmodel

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.*
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import io.reactivex.disposables.CompositeDisposable
import com.advent.tradetracker.*
import com.advent.tradetracker.model.*
import com.advent.tradetracker.view.SnoozeDialog
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import io.fabric.sdk.android.Fabric
import io.reactivex.Flowable
import timber.log.Timber

class MainActivity : com.advent.tradetracker.model.SnoozeInterface, StockInterface, AppCompatActivity() {

    private lateinit var dbFunctions: com.advent.tradetracker.model.DatabaseFunctions
    private val compositeDisposable = CompositeDisposable()
    override fun getCompositeDisposable() = compositeDisposable

    private var infoSnoozer: TextView? = null
    private var progressSnoozer: ProgressBar? = null
    override fun setSnoozeInfo(info: String) { infoSnoozer?.text = info }
    override fun setSnoozeProgress(progress: Int) { progressSnoozer?.progress = progress }
    override fun setMaxSnoozeProgress(progress: Int) { progressSnoozer?.max = progress }

    override fun addOrEditStock(stock: Stock): Boolean {
        return if (::dbFunctions.isInitialized) {
            Timber.v( "addOrEditStock called")
            dbFunctions.addOrEditStock(stock)
        } else {
            Timber.d("dbFunctions is not initialized yet; could not add or edit")
            false
        }
    }

    override fun deleteStockByStockId(stockId: Long): Boolean {
        return if (::dbFunctions.isInitialized) {
            Timber.v("deleteStockByStockId called")
            dbFunctions.deleteStockByStockId(stockId)
        } else {
            Timber.d("dbFunctions is not initialized yet; could not delete")
            false
        }
    }

    override fun getFlowingStockList(): Flowable<List<Stock>> {
        return if (::dbFunctions.isInitialized) {
            Timber.v( "getFlowingStockList called")
            dbFunctions.getFlowableStockList()
        } else {
            Timber.d("dbFunctions is not initialized yet; could not return flowable list")
            Flowable.empty()
        }
    }

    /**
     * Registers broadcast receiver, populates stock listview.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //It is recommended to separate out this logic by build instead of programmatically
        //https://medium.com/@caueferreira/timber-enhancing-your-logging-experience-330e8af97341
        //But didn't do this yet because our project structure doesn't match that in the tutorial

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
        else
            Timber.plant(ReleaseTree())

        Fabric.with(this, Crashlytics())
        Fabric.with(this, Answers())

        dbFunctions = com.advent.tradetracker.model.DatabaseFunctions(this.applicationContext)

        setContentView(R.layout.activity_main)
        infoSnoozer = findViewById(R.id.infoSnoozing)
        progressSnoozer = findViewById(R.id.snoozeProgressBar)

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            val fragment = RecyclerViewFragment()
            transaction.replace(R.id.stock_content_fragment, fragment)
            transaction.commit()
        }
        val toolbar = findViewById(R.id.cooltoolbar) as? android.support.v7.widget.Toolbar
        setSupportActionBar(toolbar)

        BatteryAwareness.registerReceiver(LocalBroadcastManager.getInstance(this), powerManager)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }

    /**
     * This menu is the "add stock, add crypto, openSnoozeDialog, settings" on top.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity_action_menu, menu)

        val switchScanningOrNot = menu?.findItem(R.id.show_scanning)?.actionView?.findViewById(R.id.show_scanning_switch) as? ToggleButton
        switchScanningOrNot?.isChecked = isMyServiceRunning(com.advent.tradetracker.NetworkService::class.java)
        val serviceIntent = Intent(this, com.advent.tradetracker.NetworkService::class.java)

        switchScanningOrNot?.setOnCheckedChangeListener { _, boo -> when(boo) {
                true -> {
                    if (!isMyServiceRunning(com.advent.tradetracker.NetworkService::class.java))
                        startService(serviceIntent)
                    else
                        toast("Scan already isRunning")
                }
                false -> { stopService(serviceIntent) }
            }
        }

        return super.onCreateOptionsMenu(menu)

    }

    /**
    * For navigation drawer in toolbar
    */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //throw RuntimeException("Hi There!")
        when (item.itemId) {
            R.id.action_settings -> startActivity<com.advent.tradetracker.SettingsActivity>()
            R.id.action_snooze -> {
                val snoozeDialog = SnoozeDialog()
                snoozeDialog.open(this, isMyServiceRunning(com.advent.tradetracker.NetworkService::class.java))
            }
            R.id.action_add_stock -> {
                val intent = Intent(this, AddEditStockActivity::class.java)
                intent.putExtra("isEditingCrypto", false)
                        .putExtra("isEditingExisting", false)
                startActivityForResult(intent, ADD_SOMETHING)
            }
            R.id.action_add_crypto -> {
                val intent = Intent(this, AddEditStockActivity::class.java)
                intent.putExtra("isEditingCrypto", true)
                        .putExtra("isEditingExisting", false)
                startActivityForResult(intent, ADD_SOMETHING)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Check which request we're responding to

        when(requestCode) {
            ADD_SOMETHING -> {
                if (resultCode == Activity.RESULT_OK) {
                    val stock = data?.getParcelableExtra<Stock>("stock")
                    if (stock != null) {
                        if (addOrEditStock(stock)) {
                            toast("Added stock ${stock.ticker} successfully")
                        } else {
                            toast("Failed to add ${stock.ticker}")
                        }
                    } else {
                        toast("Did not receive stock info back to add")
                    }
                }
            }
            EDIT_SOMETHING -> {
                if (resultCode == Activity.RESULT_OK) {
                    val stock = data?.getParcelableExtra<Stock>("stock")
                    if (stock != null) {
                        if (addOrEditStock(stock)) toast("Successfully edited ${stock.ticker}") else toast("Edit of ${stock.ticker} unsuccessful")
                    } else {
                        val stockId = data?.getLongExtra("stockIdToDelete", -555)
                        if (stockId != null) {
                            if (deleteStockByStockId(stockId)) toast("Successfully deleted stock# $stockId") else toast("Could not delete stock# $stockId")
                        } else {
                            toast("Received neither stock info back to delete, not stock info to edit")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        dbFunctions.cleanup() //Close database access
        BatteryAwareness.unregisterReceiver(LocalBroadcastManager.getInstance(this))
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if ((com.advent.tradetracker.model.SnoozeManager.isSnoozing()) && (infoSnoozer!!.text.equals(resources.getString(R.string.notsnoozing)))) {
            //It is snoozing but the UIthread that SnoozeDialog created in the background was destroyed :(
            async {
                setMaxSnoozeProgress(com.advent.tradetracker.model.SnoozeManager.snoozeMsecTotal.toInt())
                while (com.advent.tradetracker.model.SnoozeManager.isSnoozing()) {
                    uiThread {
                        setSnoozeProgress(com.advent.tradetracker.model.SnoozeManager.getSnoozeTimeRemaining().toInt())
                        setSnoozeInfo(resources.getString(R.string.snoozeremain,
                                com.advent.tradetracker.model.SnoozeManager.getSnoozeTimeRemaining().toInt(),
                                com.advent.tradetracker.model.SnoozeManager.snoozeMsecTotal))
                    }
                    com.advent.tradetracker.Utility.sleepWithThreadInterruptIfWokenUp(1000L)
                }

                uiThread {
                    setSnoozeInfo(resources.getString(R.string.notsnoozing))
                }
            }
        }
    }

    /**
     * Query the system for if a service is already isRunning.
     * @param[serviceClass] the service class
     * @return true if isRunning, false if not
     * @sample startService
     */
    @Suppress("DEPRECATION")
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Timber.i( true.toString() + "")
                return true
            }
        }
        Timber.i( false.toString() + "")
        return false
    }
}