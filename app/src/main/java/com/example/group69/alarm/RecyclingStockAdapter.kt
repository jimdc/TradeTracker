package com.example.group69.alarm

import android.content.ContentValues.TAG
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.util.Log
import android.widget.AdapterView
import android.widget.ImageView
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import org.jetbrains.anko.*

/**
 * Provide views to RecyclerView with data from RSAstocklist.
 */
public class RecyclingStockAdapter : RecyclerView.Adapter<RecyclingStockAdapter.ViewHolder> {
    final val TAG = "RecyclingStockAdapter"
    lateinit var RSAstocklist: List<Stock>
    var currentPrices: MutableMap<Long, Pair<Double,String>> = mutableMapOf()

    constructor (stocks: List<Stock>) { RSAstocklist = stocks }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        public lateinit var textView: TextView

        lateinit var Row1: TextView
        lateinit var Row2: TextView
        lateinit var Editbtn : ImageView
        lateinit var Delbtn : ImageView

        lateinit var thestock : Stock //Accursed violation of separation of concerns

        init {
            Row1 = v.findViewById(R.id.txtName)
            Row2 = v.findViewById(R.id.txtComment)
            Editbtn = v.findViewById(R.id.imgEditStock)
            Delbtn = v.findViewById(R.id.imgDeleteStock)
            v.setOnClickListener { view ->
                Log.v(TAG, "Element " + adapterPosition + " clicked.")
            }
            Editbtn.setOnClickListener { view ->
                Log.v(TAG, "Edit " + adapterPosition + " clicked.")
                with(view.context) {
                    startActivity<AddEditStockActivity>("EditingExisting" to true, "snooze" to false,
                            "EditingCrypto" to (thestock.crypto > 0), "TheStock" to thestock)
                }
            }
            Delbtn.setOnClickListener {
                view ->
                Log.v(TAG, "Delete " + adapterPosition + " clicked.")
                with(view.context) {
                    alert("Are you sure you want to delete row " + adapterPosition.toString(), "Confirm") {
                        positiveButton("Yes") {
                            if (dbsBound) {
                                if (dbService.deletestockInternal(thestock.stockid)) {
                                    toast("Successfully deleted stock.")
                                } else {
                                    toast("Could not delete stock.")
                                }
                            } else {
                                toast("Could not connect to DB service to delete stock.")
                            }
                        }
                        negativeButton("No") { toast("OK, nothing was deleted.") }
                    }.show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        var v = LayoutInflater.from(parent?.context).inflate(R.layout.user_list_row, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        Log.d(TAG, "Element " + position + " set.")

        var stock = RSAstocklist[position]
        holder?.Row1?.text = stock.toString()
        holder?.Row2?.text = "Row ${position.toString()} @ " + currentPrices[stock.stockid] ?: "not recently updated"
        holder?.thestock = stock
    }

    fun setCurrentPrice(stockid: Long, price: Double, time: String) {
        currentPrices[stockid] = Pair(price, time)
        notifyDataSetChanged()
    }

    fun refresh(newitems: List<Stock>) {
        RSAstocklist = newitems
        notifyDataSetChanged()
    }

    override fun getItemCount() = RSAstocklist.size
}