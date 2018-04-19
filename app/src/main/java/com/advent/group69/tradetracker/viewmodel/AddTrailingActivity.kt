package com.advent.group69.tradetracker.viewmodel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.support.design.widget.FloatingActionButton
import android.view.View
import org.jetbrains.anko.*
import java.util.Calendar
import android.widget.*
import com.advent.group69.tradetracker.model.Stock
import android.app.Activity
import android.content.Intent
import com.advent.group69.tradetracker.R
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import io.reactivex.subjects.PublishSubject
import android.widget.EditText
import com.advent.group69.tradetracker.NetworkService
import com.advent.group69.tradetracker.R.id.stopLoss
import com.advent.group69.tradetracker.R.id.trailingPercent
import com.advent.group69.tradetracker.SettingsActivity
import io.reactivex.Observable

class AddTrailingActivity : AppCompatActivity() {

    private lateinit var tickerName: EditText
    private lateinit var activationPrice: EditText
    private lateinit var trailingPercent: EditText
    private lateinit var stopLoss: EditText
    private lateinit var phoneChecked: CheckBox
    private lateinit var btnDelete: Button


    private var workingStock = Stock(-1, "Default", -1.0, -1.0, -1.0, -1.0, -1.0, 0L, 0L, 0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_stock)

        tickerName = findViewById(R.id.tickerName)
        phoneChecked = findViewById(R.id.phoneCallCB)
        btnDelete = findViewById(R.id.delbtn)
        activationPrice = findViewById(R.id.activationPrice)
        trailingPercent = findViewById(R.id.trailingPercent)
        stopLoss = findViewById(R.id.stopLoss)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.target_price -> startActivity<AddEditStockActivity>()
            R.id.trailing_stop -> startActivity<AddTrailingActivity>()
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

    override fun onStart() {
        super.onStart()

        val bundleFromIntent = intent.extras
        val isEditingCrypto: Boolean? = bundleFromIntent.getBoolean("isEditingCrypto")
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
                workingStock.target = 0.0
                workingStock.stopLoss = stockFromView.stopLoss
                workingStock.trailingPercent = stockFromView.trailingPercent
                workingStock.activationPrice = stockFromView.activationPrice
                workingStock.highestPrice = stockFromView.highestPrice
                workingStock.above = stockFromView.above
                workingStock.crypto = stockFromView.crypto
            }


            title = resources.getString(R.string.title_activity_edit_stock, workingStock.ticker)

            toast("HP: " + workingStock.highestPrice.toString() + " trail: " + workingStock.trailingPercent.toString())


            tickerName.setText(workingStock.ticker)
            trailingPercent.setText(workingStock.trailingPercent.toString())
            activationPrice.setText(workingStock.activationPrice.toString())
            stopLoss.setText(workingStock.stopLoss.toString())
            phoneChecked.isChecked = workingStock.phone > 0L

            btnDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener(deleteStockClickListener)
        } else if (isEditingExisting == false) {

            workingStock.stockid = Calendar.getInstance().timeInMillis
            if (isEditingCrypto == true) workingStock.crypto = 1L
            else workingStock.crypto = 0L

            title =
                    if (isEditingCrypto == true) resources.getString(R.string.title_activity_add_crypto)
                    else resources.getString(R.string.title_activity_add_stock)

            btnDelete.visibility = View.INVISIBLE
        }
    }

    private fun isUserInputNotEmpty(): Boolean {
        return !(tickerName.text.trim().isEmpty() or trailingPercent.text.trim().isEmpty())
    }

    private fun updateWorkingStockFromUserInput(): Boolean {

        if (!isUserInputNotEmpty()) return false

        workingStock.ticker = tickerName.text.toString()
        workingStock.trailingPercent = trailingPercent.text.toString().toDoubleOrNull() ?: -1.0
        workingStock.activationPrice = activationPrice.text.toString().toDoubleOrNull() ?: -1.0
        workingStock.stopLoss = stopLoss.text.toString().toDoubleOrNull() ?: -1.0
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

    fun getTextWatcherObservable(editText: EditText): Observable<String> {

        val subject = PublishSubject.create<String>()

        editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable) {
                subject.onNext(s.toString())
            }

        })

        return subject
    }

}