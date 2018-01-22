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

        val toolbar = find<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val tickerName = find<EditText>(R.id.tickerName)
        val tickerPrice = find<EditText>(R.id.tickerPrice)
        val aboveChecked = find<RadioButton>(R.id.rbAbove)
        val phoneChecked = find<CheckBox>(R.id.phoneCallCB)
        val addbutton = find<FloatingActionButton>(R.id.fab)
        val deletebutton = find<Button>(R.id.delbtn)

        val b = intent.extras
        var stockid = Calendar.getInstance().getTimeInMillis()

        val EditingCrypto = b.getBoolean("EditingCrypto")
        val EditingExisting = b.getBoolean("EditingExisting")
        if (EditingExisting) {

            val thestock: Stock = b.getParcelable("TheStock")
            stockid = thestock.stockid
            val stockticker = thestock.ticker

            setTitle(getResources().getString(R.string.title_activity_edit_stock, stockticker))

            tickerName.setText(stockticker)
            tickerPrice.setText(thestock.target.toString())
            aboveChecked.setChecked(thestock.above < 1)
            phoneChecked.setChecked(thestock.phone < 1)

            deletebutton.setOnClickListener { view ->
                var nraffected: Int = 0

                database.use {
                    nraffected = delete("TableView2", "_stockid=$stockid")
                }

                if (nraffected == 1) {
                    toast(getResources().getString(R.string.numdeleted, stockticker))
                } else if (nraffected == 0) {
                    toast(getResources().getString(R.string.delfail))
                }

                finish()
            }
        } else { //adding a new stock
            if (EditingCrypto) {
                setTitle(getResources().getString(R.string.title_activity_add_crypto))
            }

            deletebutton.visibility = View.INVISIBLE
        }

        addbutton.setOnClickListener { view ->
            val stockaddrequest = StockProposalValidationRequest(this)

            val target: Double? = tickerPrice.text.toString().toDoubleOrNull()
            var editedstock = Stock(stockid, tickerName.text.toString(),
                    target ?: 6.66, aboveChecked.isChecked, phoneChecked.isChecked, EditingCrypto)

            stockaddrequest.execute(editedstock)
            finish()
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}
