package com.example.group69.alarm

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.support.v7.widget.AppCompatEditText
import android.view.View
import android.widget.*
import org.jetbrains.anko.*
import java.util.Calendar
import org.jetbrains.anko.db.delete
import java.io.IOException
import android.util.Log

class AddEditStockActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_stock)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val tickerName = findViewById(R.id.tickerName) as EditText
        val tickerPrice = findViewById(R.id.tickerPrice) as EditText
        val aboveChecked = findViewById(R.id.rbAbove) as RadioButton
        val phoneChecked = findViewById(R.id.phoneCallCB) as CheckBox
        val addbutton = findViewById(R.id.fab) as FloatingActionButton
        val deletebutton = findViewById(R.id.delbtn) as Button

        val b = intent.extras
        var stockid = Calendar.getInstance().getTimeInMillis()

        val EditingExisting = b.getBoolean("EditingExisting")
        if (EditingExisting) {

            val thestock : Stock = b.getParcelable("TheStock")
            stockid = thestock.stockid
            val stockticker = thestock.ticker

            setTitle(getResources().getString(R.string.title_activity_edit_stock, stockticker))

            tickerName.setText(stockticker)
            tickerPrice.setText(thestock.target.toString())
            aboveChecked.setChecked(thestock.above < 1)
            phoneChecked.setChecked(thestock.phone < 1)

            deletebutton.setOnClickListener { view ->
                var nraffected : Int = 0

                database.use {
                    nraffected = delete("Portefeuille", "_stockid=$stockid")
                }

                if (nraffected == 1) {
                    toast(getResources().getString(R.string.numdeleted, stockticker))
                } else if (nraffected == 0) {
                    toast(getResources().getString(R.string.delfail))
                }

                finish()
            }
        } else { //adding a new stock
            deletebutton.visibility = View.INVISIBLE
        }

        addbutton.setOnClickListener { view ->
            val stockaddrequest = StockProposalValidationRequest(this)

            val target: Double? = tickerPrice.text.toString().toDoubleOrNull()
            var editedstock = Stock(stockid, tickerName.text.toString(),
                    target ?: 6.66, aboveChecked.isChecked, phoneChecked.isChecked)

            stockaddrequest.execute(editedstock)
            finish()
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}
