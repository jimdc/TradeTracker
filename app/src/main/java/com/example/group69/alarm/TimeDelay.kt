package com.example.group69.alarm

import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.support.v7.widget.AppCompatEditText
import android.util.Log
import android.view.View
import android.widget.*
import org.jetbrains.anko.*
import org.jetbrains.anko.db.delete
import java.util.*
//import jdk.nashorn.internal.objects.NativeDate.getYear


class TimeDelay : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timedelay)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val tickerName = findViewById(R.id.tickerName) as EditText
        val tickerPrice = findViewById(R.id.tickerPrice) as EditText
        val aboveChecked = findViewById(R.id.rbAbove) as RadioButton
        val phoneChecked = findViewById(R.id.phoneCallCB) as CheckBox
        val fab = findViewById(R.id.fab) as FloatingActionButton

        val b = intent.extras
        val EditingExisting = b.getBoolean("EditingExisting")
        if (EditingExisting) {

            val thestock : Stock = b.getParcelable("TheStock")
            val stockid = thestock.stockid
            val stockticker = thestock.ticker

            setTitle(getResources().getString(R.string.title_activity_edit_stock, stockticker))

            tickerName.setText(stockticker)
            tickerPrice.setText(thestock.target.toString())

            if (thestock.above < 1) { //Mindful of both cases since the default can change.
                aboveChecked.setChecked(false)
            } else {
                aboveChecked.setChecked(true)
            }

            if (thestock.phone < 1) {
                phoneChecked.setChecked(false)
            } else {
                phoneChecked.setChecked(true)
            }

            val delbtn = findViewById(R.id.delbtn) as Button

            delbtn.setOnClickListener { view ->
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

            fab.setOnClickListener { view ->

                var tickPrice = tickerPrice.text.toString()
                val target: Double? = tickPrice.toDoubleOrNull()

                var editedstock = Stock(thestock.stockid, tickerName.text.toString(),
                        target ?: 6.66, aboveChecked.isChecked, phoneChecked.isChecked)

                var rownum: Long = 666
                database.use {
                    rownum = replace("Portefeuille", null, editedstock.ContentValues())
                }

                var result = getResources().getString(R.string.fail2edit)
                if (rownum != -1L) {
                    result = getResources().getString(R.string.editsuccess) +
                            "#${rownum}: " + editedstock.toString()
                }

                Snackbar.make(view, result, Snackbar.LENGTH_LONG).setAction("Action", null).show()

                finish()
            }

        } else { //Adding a new stock from scratch.
            val delbtn = findViewById(R.id.delbtn) as Button
            delbtn.visibility = View.INVISIBLE

            fab.setOnClickListener { view ->

                var tickPrice = tickerPrice.text.toString()
                val target: Double? = tickPrice.toDoubleOrNull()
                var time : String = tickerName.text.toString()
                //broadcast to the updaten thread to make it sleep

                val intent = Intent("com.example.group69.alarm")
                intent.putExtra("delay",time) //should send the stock, price, and number so we know which to delete on the UI display
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                TimeZone.getTimeZone("EST")

                Log.d("sleeping", Date().hours.toString())
                //if(Date().time < )
                finish()
            }
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}
