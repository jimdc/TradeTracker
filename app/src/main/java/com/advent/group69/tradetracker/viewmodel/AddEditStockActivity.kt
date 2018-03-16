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


const val ADD_SOMETHING = 1
const val EDIT_SOMETHING = 2

class AddEditStockActivity : AppCompatActivity() {

    private lateinit var stockticker: String
    private lateinit var tickerName: EditText
    private lateinit var tickerPrice: EditText
    private lateinit var aboveChecked: RadioButton
    private lateinit var belowChecked: RadioButton
    private lateinit var phoneChecked: CheckBox
    private lateinit var btnDelete: Button

    private var workingStock = Stock(-1, "Default", -1.0, -1.0, -1.0, -1.0, -1.0, 0L, 0L, 0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_stock)

        tickerName = findViewById(R.id.tickerName)
        tickerPrice = findViewById(R.id.tickerPrice)
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
                workingStock.target = stockFromView.target
                workingStock.percent = stockFromView.percent
                workingStock.stopLossPercent = stockFromView.stopLossPercent
                workingStock.activationPrice = stockFromView.activationPrice
                workingStock.highestPrice = stockFromView.highestPrice
                workingStock.above = stockFromView.above
                workingStock.crypto = stockFromView.crypto
            }


            title = resources.getString(R.string.title_activity_edit_stock, workingStock.ticker)

            tickerName.setText(workingStock.ticker)
            tickerPrice.setText(workingStock.target.toString())
            phoneChecked.isChecked = workingStock.phone > 0L
            if (workingStock.above > 0L) aboveChecked.isChecked = true else belowChecked.isChecked = true

            btnDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener(deleteStockClickListener)
        } else if (isEditingExisting == false) {

            workingStock.stockid = Calendar.getInstance().timeInMillis

            title =
                    if (isEditingCrypto == true) resources.getString(R.string.title_activity_add_crypto)
                    else resources.getString(R.string.title_activity_add_stock)

            btnDelete.visibility = View.INVISIBLE
        }
    }

    private fun isUserInputNotEmpty(): Boolean {
        return !(tickerName.text.trim().isEmpty() or tickerPrice.text.trim().isEmpty())
    }

    private fun updateWorkingStockFromUserInput(): Boolean {

        if (!isUserInputNotEmpty()) return false

        workingStock.ticker = tickerName.text.toString()
        workingStock.target = tickerPrice.text.toString().toDoubleOrNull() ?: -1.0
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
}