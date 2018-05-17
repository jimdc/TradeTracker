package com.advent.tradetracker.viewmodel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.support.design.widget.FloatingActionButton
import android.view.View
import org.jetbrains.anko.*
import java.util.Calendar
import android.widget.*
import com.advent.tradetracker.model.Stock
import android.app.Activity
import android.content.Intent
import com.advent.tradetracker.R
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import io.reactivex.subjects.PublishSubject
import android.widget.EditText
import com.advent.tradetracker.Utility.isValidTickerSymbol
import com.advent.tradetracker.NetworkService
import com.advent.tradetracker.R.id.stopLoss
import com.advent.tradetracker.R.id.trailingPercent
import com.advent.tradetracker.SettingsActivity
import com.advent.tradetracker.view.SnoozeDialog
import io.reactivex.Observable
import android.widget.Toast
import android.R.id.button2
import android.R.id.button1
import com.advent.tradetracker.model.Cryptocurrency
import android.graphics.Color
import io.reactivex.rxkotlin.Observables
import timber.log.Timber


const val ADD_SOMETHING = 1
const val EDIT_SOMETHING = 2

class AddEditStockActivity : AppCompatActivity() {

    private lateinit var tickerName: EditText
    private lateinit var tickerPrice: EditText
    private lateinit var tickerObservable: Observable<String>
    private lateinit var targetObservable: Observable<String>
    private lateinit var isUserInputNotEmpty: Observable<Boolean>
    private lateinit var tickerHelper: TextView
    private lateinit var targetHelper: TextView

    private lateinit var aboveChecked: RadioButton
    private lateinit var belowChecked: RadioButton
    private lateinit var phoneChecked: CheckBox
    private lateinit var btnDelete: Button
    private var trailStop = false
    private var isEditingCrypt : Boolean? = false


    private var workingStock = Stock(-1, "Default", -1.0, -1.0, -1.0, -1.0, -1.0, 0L, 0L, 0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_stock)

        tickerName = findViewById(R.id.tickerName)
        tickerPrice = findViewById(R.id.tickerPrice)
        tickerHelper = findViewById(R.id.tickerHelper)
        targetHelper = findViewById(R.id.targetHelper)
        tickerObservable = getTextWatcherObservable(tickerName)
        targetObservable = getTextWatcherObservable(tickerPrice)

        fun onNextTickerValidate(t: String) {
            val tRimmed = t.trim()
            if (tRimmed.isEmpty()) {
                tickerHelper.text = "Empty"
                tickerHelper.setTextColor(Color.MAGENTA)
            } else {
                if (!tRimmed.isValidTickerSymbol()) {
                    tickerHelper.text = "Malformed"
                    tickerHelper.setTextColor(Color.RED)
                } else {
                    tickerHelper.text = "OK"
                    tickerHelper.setTextColor(Color.GREEN)
                }
            }
        }
        fun onNextTargetBetterNotBeEmpty(p: String) {
            if (p.isEmpty()) {
                targetHelper.text = "Empty"
                targetHelper.setTextColor(Color.MAGENTA)
            } else {
                targetHelper.text = "OK"
                targetHelper.setTextColor(Color.GREEN)
            }
        }
        tickerObservable.subscribe({ t -> onNextTickerValidate(t) })
        targetObservable.subscribe({ p -> onNextTargetBetterNotBeEmpty(p) })

        isUserInputNotEmpty = Observables.combineLatest(
                tickerObservable, targetObservable) { t: String, p: String -> t.isNotEmpty() && p.isNotEmpty() }

        aboveChecked = findViewById(R.id.rbAbove)
        belowChecked = findViewById(R.id.rbBelow)
        phoneChecked = findViewById(R.id.phoneCallCB)
        btnDelete = findViewById(R.id.delbtn)
        val btnAdd = findViewById<FloatingActionButton>(R.id.fab)
        btnAdd.setOnClickListener(stockClickListener)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.scan_type_action_menu, menu)
        return super.onCreateOptionsMenu(menu)

    }
 /*   override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.target_price -> {
                val intent = Intent(this, AddEditStockActivity::class.java)
                intent.putExtra("isEditingCrypto", false)
                        .putExtra("isEditingExisting", false)
                startActivityForResult(intent, ADD_SOMETHING)
            }
            R.id.trailing_stop -> {
                val intent = Intent(this, AddTrailingActivity::class.java)
                intent.putExtra("isEditingCrypto", false)
                        .putExtra("isEditingExisting", false)
                startActivityForResult(intent, ADD_SOMETHING)
                //finish()
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
    } */


    override fun onStart() {
        super.onStart()

        val bundleFromIntent = intent.extras
        val isEditingCrypto: Boolean? = bundleFromIntent.getBoolean("isEditingCrypto")
        isEditingCrypt = isEditingCrypto
        val isEditingExisting: Boolean? = bundleFromIntent.getBoolean("isEditingExisting")
        if ((isEditingCrypto == null) or (isEditingExisting == null)) {
            toast("Did not receive isEditingCrypto or isEditingExisting from MainActivity")
            finish()
        }

        if (isEditingExisting == true) {

            val stockFromView: Stock? = bundleFromIntent.getParcelable("TheStock")
            if (stockFromView == null) {
                toast("Did not receive stock to edit from MainActivity")
                finish()
            } else {
                workingStock.stockid = stockFromView.stockid
                workingStock.ticker = stockFromView.ticker
                workingStock.target = stockFromView.target
                workingStock.above = stockFromView.above
                workingStock.crypto = stockFromView.crypto
            }


            title = resources.getString(R.string.title_activity_edit_stock, workingStock.ticker)

            toast("HP: " + workingStock.highestPrice.toString() + " trail: " + workingStock.trailingPercent.toString())


            tickerName.setText(workingStock.ticker)
            tickerPrice.setText(workingStock.target.toString())
            phoneChecked.isChecked = workingStock.phone > 0L
            if (workingStock.above > 0L) aboveChecked.isChecked = true else belowChecked.isChecked = true

            btnDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener(deleteStockClickListener)
        } else if (isEditingExisting == false) {

            workingStock.stockid = Calendar.getInstance().timeInMillis
            if (isEditingCrypto == true) workingStock.crypto = 1L
            else workingStock.crypto = 0L

            if (isEditingCrypto == true) workingStock.crypto = 1L
            else workingStock.crypto = 0L

            title =
                    if (isEditingCrypto == true) resources.getString(R.string.title_activity_add_crypto)
                    else resources.getString(R.string.title_activity_add_stock)

            btnDelete.visibility = View.INVISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.menubtn -> {

                // THE R.id.button2 has to be the same as the item that will trigger the popup menu.
                val v = findViewById<View>(R.id.menubtn)
                val pm = PopupMenu(this@AddEditStockActivity, v)
                pm.menuInflater.inflate(R.menu.more_buttons, pm.menu)
                pm.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        when (item.itemId) {

                            R.id.target_price -> {

                            }

                            R.id.trailing_stop -> {
                                val intent = Intent(applicationContext, AddTrailingActivity::class.java)
                                intent.putExtra("isEditingCrypto", isEditingCrypt)
                                        .putExtra("isEditingExisting", false)
                                startActivityForResult(intent, ADD_SOMETHING)

                            }
                            else -> {
                                Timber.d("error")
                            }
                        }
                        return true
                    }
                } )
                pm.show()
            }
            else -> {
                Timber.d("error2")
            }

        }
        return false
    }

    private fun getTextWatcherObservable(editText: EditText): Observable<String> {
        val subject = PublishSubject.create<String>()

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable) {
                subject.onNext(s.toString())
            }
        })

        return subject
    }

    private fun updateWorkingStockFromUserInput(): Boolean {

        //if (isUserInputNotEmpty.blockingLast() == false) return false

        workingStock.ticker = tickerName.text.toString().trim()
        workingStock.target = tickerPrice.text.toString().toDoubleOrNull() ?: -1.0
        //workingStock.trailingPercent = trailingPercent.text.toString().toDoubleOrNull() ?: -1.0
        //workingStock.activationPrice = activationPrice.text.toString().toDoubleOrNull() ?: -1.0
        //workingStock.stopLoss = stopLoss.text.toString().toDoubleOrNull() ?: -1.0
        workingStock.above = if (aboveChecked.isChecked) 1L else 0L
        workingStock.phone = if (phoneChecked.isChecked) 1L else 0L

        return (workingStock.target != -1.0)
    }

    private val stockClickListener = View.OnClickListener {
        if (updateWorkingStockFromUserInput()) {
            val resultIntent = Intent()
            resultIntent.putExtra("stock", workingStock)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            toast("Your inputs don't look correct, so I won't even try to add/edit.")
        }
    }

    private val deleteStockClickListener = View.OnClickListener {
        val resultIntent = Intent()
        resultIntent.putExtra("stockIdToDelete", workingStock.stockid)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val stock = data?.getParcelableExtra<Stock>("stock")

        if (stock != null) {
            val resultIntent = Intent()
            resultIntent.putExtra("stock", stock)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            Timber.d("Did not receive stock info back to add")
        }
    }

}