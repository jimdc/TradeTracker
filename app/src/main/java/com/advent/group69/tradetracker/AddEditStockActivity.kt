package com.advent.group69.tradetracker

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.support.design.widget.FloatingActionButton
import android.app.ActionBar
import android.view.View
import org.jetbrains.anko.*
import java.util.Calendar
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.*
import com.advent.group69.tradetracker.model.Stock


class AddEditStockActivity : AppCompatActivity() {

    /**
     * Customizes the UI based on intent extras "isEditingCrypto" and "isEditingExisting"
     */
    private var isEditingCrypto: Boolean = false
    private var isEditingExisting: Boolean = false
    private var stockid = Calendar.getInstance().timeInMillis
    private lateinit var stockticker: String
    private lateinit var tickerName: EditText
    private lateinit var tickerPrice: EditText
    private lateinit var aboveChecked: RadioButton
    private lateinit var phoneChecked: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_stock)

        tickerName = findViewById(R.id.tickerName)
        tickerPrice = findViewById(R.id.tickerPrice)
        aboveChecked = findViewById(R.id.rbAbove)
        phoneChecked = findViewById(R.id.phoneCallCB)

        val deleteButton = findViewById<Button>(R.id.delbtn)
        val bundleFromIntent = intent.extras
        isEditingCrypto = bundleFromIntent.getBoolean("isEditingCrypto")
        isEditingExisting = bundleFromIntent.getBoolean("isEditingExisting")
        if (isEditingExisting) {
            val stockFromView: Stock? = bundleFromIntent.getParcelable("TheStock")
            if (stockFromView == null) {
                toast("Did not receive stock to edit from MainActivity")
                finish()
            } else {
                stockid = stockFromView.stockid
                val stockTicker = stockFromView.ticker

                title = resources.getString(R.string.title_activity_edit_stock, stockTicker)

                tickerName.setText(stockTicker)
                tickerPrice.setText(stockFromView.target.toString())
                aboveChecked.isChecked = stockFromView.above > 0
                phoneChecked.isChecked = stockFromView.phone > 0

                deleteButton.setOnClickListener(DeleteStockClickListener)
            }
        } else if (!isEditingExisting) { //adding a new stock
            if (isEditingCrypto) {
                title = resources.getString(R.string.title_activity_add_crypto)
            }
            deleteButton.visibility = View.INVISIBLE //Why not let us delete it?
        }

        val addbutton = findViewById<FloatingActionButton>(R.id.fab)
        addbutton.setOnClickListener(stockClickListener)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val inflater = supportActionBar?.themedContext?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val customActionBarView = inflater?.inflate(R.layout.actionbar_custom_view_done_cancel, null)

        customActionBarView?.findViewById<FrameLayout>(R.id.actionbar_done)?.setOnClickListener(stockClickListener) //"Done"
        customActionBarView?.findViewById<FrameLayout>(R.id.actionbar_cancel)?.setOnClickListener({ finish() }) // "Cancel"

        // Show the custom action bar view and hide the normal Home icon and title.
        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_SHOW_TITLE)
        supportActionBar?.setCustomView(customActionBarView, Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    /**
     * Used BOTH by the "Add" button on bottom right, and the "Done" button in toolbar.
     * @todo Validate content
     */
    private val stockClickListener = View.OnClickListener {
        val target: Double? = tickerPrice.text.toString().toDoubleOrNull()

        val editedStock = Stock(
                stockid,
                tickerName.text.toString(),
                target ?: 6.66, -1.0, -1.0, -1.0, -1.0,
                aboveChecked.isChecked,
                phoneChecked.isChecked,
                isEditingCrypto
        )
        dbFunctions.addOrEditStock(editedStock)
        finish()
    }

    private val DeleteStockClickListener = View.OnClickListener {
        if (dbFunctions.deleteStockByStockId(stockid)) toast(resources.getString(R.string.numdeleted, stockticker))
        else toast(resources.getString(R.string.delfail))

        finish()
    }
}
