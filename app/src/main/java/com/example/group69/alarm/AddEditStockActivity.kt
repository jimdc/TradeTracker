package com.example.group69.alarm

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.support.design.widget.FloatingActionButton
import android.app.ActionBar
import android.util.Log
import android.view.View
import org.jetbrains.anko.*
import java.util.Calendar
import org.jetbrains.anko.db.delete
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.*


class AddEditStockActivity : AppCompatActivity() {

    /**
     * Customizes the UI based on intent extras "EditingCrypto" and "EditingExisting"
     * @todo make more modular by having Datenbank interaction in own function
     */
    var Snooze: Boolean = false
    var EditingCrypto: Boolean = false
    var EditingExisting: Boolean = false
    var stockid = Calendar.getInstance().getTimeInMillis() //@todo use autoincrement
    lateinit var stockticker: String
    lateinit var tickerName: EditText
    lateinit var tickerPrice: EditText
    lateinit var aboveChecked: RadioButton
    lateinit var phoneChecked: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_stock)

        tickerName = findViewById(R.id.tickerName)
        tickerPrice = findViewById(R.id.tickerPrice)
        aboveChecked = findViewById(R.id.rbAbove)
        phoneChecked = findViewById(R.id.phoneCallCB)

        val deletebutton = findViewById(R.id.delbtn) as Button
        val b = intent.extras
        EditingCrypto = b.getBoolean("EditingCrypto")
        EditingExisting = b.getBoolean("EditingExisting")
        Snooze = b.getBoolean("snooze")
        if (Snooze) {
            setTitle("Enter minutes for snooze for price")
            deletebutton.visibility = View.INVISIBLE
        } else if (EditingExisting) {
            val thestock: Stock = b.getParcelable("TheStock")
            stockid = thestock.stockid
            val stockticker = thestock.ticker

            setTitle(resources.getString(R.string.title_activity_edit_stock, stockticker))

            tickerName.setText(stockticker)
            tickerPrice.setText(thestock.target.toString())
            aboveChecked.setChecked(thestock.above < 1)
            phoneChecked.setChecked(thestock.phone < 1)

            deletebutton.setOnClickListener(DeleteStockClickListener)
        } else if (!EditingExisting) { //adding a new stock
            if (EditingCrypto) { setTitle(getResources().getString(R.string.title_activity_add_crypto)) }
            deletebutton.visibility = View.INVISIBLE
        }

        val addbutton = findViewById(R.id.fab) as FloatingActionButton
        addbutton.setOnClickListener(AddStockClickListener)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val inflater = supportActionBar?.themedContext?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val customActionBarView = inflater?.inflate(R.layout.actionbar_custom_view_done_cancel, null)

        customActionBarView?.findViewById<FrameLayout>(R.id.actionbar_done)?.setOnClickListener(AddStockClickListener) //"Done"
        customActionBarView?.findViewById<FrameLayout>(R.id.actionbar_cancel)?.setOnClickListener(View.OnClickListener { finish() }) // "Cancel"

        // Show the custom action bar view and hide the normal Home icon and title.
        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_SHOW_TITLE)
        supportActionBar?.setCustomView(customActionBarView, Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    /**
     * Used BOTH by the "Add" button on bottom right, and the "Done" button in toolbar.
     * @todo Do not add default values; i.e., validate content
     */
    private val AddStockClickListener = object : View.OnClickListener {
        override fun onClick(v: View) {
            val target: Double? = tickerPrice.text.toString().toDoubleOrNull()

            val editedstock = if(Snooze) { Stock(stockid, "snoozee", target
                    ?: 6.66, aboveChecked.isChecked, phoneChecked.isChecked, EditingCrypto) } else {
                Stock(stockid, tickerName.text.toString(), target
                        ?: 6.66, aboveChecked.isChecked, phoneChecked.isChecked, EditingCrypto)
            }

            if (dbsBound) { dbService.addeditstock(editedstock) }
            else { Log.e("AddButton", "OnClickListener: dbsBound = false, so did nothing.") }

            finish()
        }
    }

    /*
     * @todo: handle case where stockticker is not fulfilled
     */
    private val DeleteStockClickListener = object : View.OnClickListener {
        override fun onClick(v: View) {
            var delsuccess: Boolean = false
            if (dbsBound) { delsuccess = dbService.deletestockInternal(stockid) }

            if (delsuccess) toast(resources.getString(R.string.numdeleted, stockticker))
            else toast(resources.getString(R.string.delfail))

            finish()
        }
    }
}
