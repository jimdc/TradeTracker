package com.advent.group69.tradetracker.viewmodel

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.jetbrains.anko.*
import android.support.v4.content.LocalBroadcastManager
import android.content.IntentFilter
import android.support.v7.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import io.reactivex.disposables.CompositeDisposable
import android.os.Build
import com.advent.group69.tradetracker.*
import com.advent.group69.tradetracker.BatteryAwareness.isPowerSavingOn
import com.advent.group69.tradetracker.model.DatabaseFunctions
import com.advent.group69.tradetracker.model.SnoozeInterface
import com.advent.group69.tradetracker.model.Stock
import com.advent.group69.tradetracker.model.StockInterface
import com.advent.group69.tradetracker.view.SnoozeDialog
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import io.fabric.sdk.android.Fabric
import io.reactivex.Flowable

class MainActivity : SnoozeInterface, StockInterface, AppCompatActivity() {

    private lateinit var dbFunctions: DatabaseFunctions
    private val compositeDisposable = CompositeDisposable()
    override fun getCompositeDisposable() = compositeDisposable

    private var infoSnoozer: TextView? = null
    private var progressSnoozer: ProgressBar? = null
    override fun setSnoozeInfo(info: String) { infoSnoozer?.text = info }
    override fun setSnoozeProgress(progress: Int) { progressSnoozer?.progress = progress }
    override fun setMaxSnoozeProgress(progress: Int) { progressSnoozer?.max = progress }

    override fun addOrEditStock(stock: Stock): Boolean {
        return if (::dbFunctions.isInitialized) {
            dbFunctions.addOrEditStock(stock)
        } else {
            Log.d("MainActivity", "dbFunctions is not initialized yet; could not add or edit")
            false
        }
    }

    override fun deleteStockByStockId(stockId: Long): Boolean {
        return if (::dbFunctions.isInitialized)
            dbFunctions.deleteStockByStockId(stockId)
        else {
            Log.d("MainActivity","dbFunctions is not initialized yet; could not delete")
            false
        }
    }

    override fun getFlowingStockList(): Flowable<List<Stock>> {
        return if (::dbFunctions.isInitialized)
            dbFunctions.getFlowableStockList()
        else {
            Log.d("MainActivity","dbFunctions is not initialized yet; could not return flowable list")
            Flowable.empty()
        }
    }

    /**
     * Registers broadcast receiver, populates stock listview.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        Fabric.with(this, Answers())

        dbFunctions = DatabaseFunctions(this.applicationContext)

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

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(BatteryAwareness.powerSaverOffPleaseReceiver, IntentFilter(BatteryAwareness.INTENT_FILTER))
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && powerManager.isPowerSaveMode) {
            isPowerSavingOn = true
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }

    /**
     * This menu is the "add stock, add crypto, openSnoozeDialog, settings" on top.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity_action_menu, menu)

        val mSwitchScanningOrNot = menu?.findItem(R.id.show_scanning)?.actionView?.findViewById(R.id.show_scanning_switch) as? ToggleButton
        mSwitchScanningOrNot?.isChecked = isMyServiceRunning(NetworkService::class.java)
        val serviceIntent = Intent(this, NetworkService::class.java)

        mSwitchScanningOrNot?.setOnCheckedChangeListener { _, boo -> when(boo) {
                true -> {
                    if (!isMyServiceRunning(NetworkService::class.java))
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
        when (item.itemId) {
            R.id.action_settings -> startActivity<SettingsActivity>()
            R.id.action_snooze -> {
                val snoozeDialog = SnoozeDialog()
                snoozeDialog.open(this, isMyServiceRunning(NetworkService::class.java))
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
                        if (addOrEditStock(stock)) toast("Added stock ${stock.ticker} successfully") else toast("Failed to add ${stock.ticker}")
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
                Log.i("isMyServiceRunning?", true.toString() + "")
                return true
            }
        }
        Log.i("isMyServiceRunning?", false.toString() + "")
        return false
    }


    override fun onDestroy() {
        dbFunctions.cleanup() //Close database access
        LocalBroadcastManager.getInstance(this).unregisterReceiver(BatteryAwareness.powerSaverOffPleaseReceiver)
        super.onDestroy()
    }
}