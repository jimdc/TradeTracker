package com.example.group69.alarm

import android.content.ContentValues.TAG
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.util.Log
import android.widget.AdapterView
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import org.jetbrains.anko.*

/**
 * Provide views to RecyclerView with data from RSAstocklist.
 */
public class RecyclingStockAdapter : RecyclerView.Adapter<RecyclingStockAdapter.ViewHolder> {
    final val TAG = "RecyclingStockAdapter"
    lateinit var RSAstocklist: List<Stock>
    var currentPrices: MutableMap<Long, Double> = mutableMapOf()

    constructor (stocks: List<Stock>) { RSAstocklist = stocks }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        public lateinit var textView: TextView

        lateinit var Row1: TextView
        lateinit var Row2: TextView

        init {
            Row1 = v.findViewById(R.id.txtName)
            Row2 = v.findViewById(R.id.txtComment)
            v.setOnClickListener { view ->
                Log.v(TAG, "Element " + adapterPosition + " clicked.")
            }
        }
    }

    private val StockviewClickListener = object : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, pos: Int, id: Long) {

            /**
             * Wrong way to do this. the ImageViews within this view have their own click listener
             * https://stackoverflow.com/questions/8571166/click-imageview-within-a-listview-listitem-and-get-the-position
            when(view.) {
            R.id.imgEditStock -> toast("Edit")
            R.id.imgDeleteStock -> toast("Delete")
            }*/

            Log.v(TAG, "Element " + pos + " clicked.")

            /*alert("Are you sure you want to delete row " + pos.toString(), "Confirm") {
                positiveButton("Yes") {
                    toast("Row " + pos.toString() +
                            if (deletestock(pos) == true) (" deleted.") else " not deleted.")
                }
                negativeButton("No") { toast("OK, nothing was deleted.") }
            }.show()*/
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
        holder?.Row2?.text = "Click to delete row # ${position.toString()} @${currentPrices[stock.stockid]}"
    }

    fun setCurrentPrice(stockid: Long, price: Double) {
        currentPrices[stockid] = price
        notifyDataSetChanged()
    }

    fun refresh(newitems: List<Stock>) {
        RSAstocklist = newitems
        notifyDataSetChanged()
    }

    override fun getItemCount() = RSAstocklist.size
}