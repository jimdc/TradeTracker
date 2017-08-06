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

class AddStockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_stock)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->

            val tickerName = findViewById(R.id.tickerName) as EditText
            val tickerPrice = findViewById(R.id.tickerPrice) as EditText
            val aboveChecked = findViewById(R.id.rbAbove) as RadioButton
            val phoneChecked = findViewById(R.id.phoneCallCB) as CheckBox

            var tickPrice = tickerPrice.text.toString()
            val target: Double? = tickPrice.toDoubleOrNull()

            var newstock = Stock(java.util.GregorianCalendar().timeInMillis, tickerName.text.toString(),
                    target ?: 6.66, aboveChecked.isChecked, phoneChecked.isChecked)

            var rownum: Long = 666
            database.use {
                rownum = replace("Portefeuille", null, newstock.ContentValues())
            }

            var result = getResources().getString(R.string.fail2add)
            if (rownum != -1L) {
                result = getResources().getString(R.string.addsuccess) + "#${rownum}: " + newstock.toString()
            }

            Snackbar.make(view, result, Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}
