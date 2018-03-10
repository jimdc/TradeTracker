package com.advent.group69.tradetracker

import android.content.ContentValues.TAG
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.util.Log
import android.widget.ImageView
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import org.jetbrains.anko.*

/**
 * Provide views to RecyclerView with data from RSAstocklist.
 */
public class RecyclingStockAdapter(stocks: List<Stock>) : RecyclerView.Adapter<RecyclingStockAdapter.ViewHolder>() {
    val TAG = "RecyclingStockAdapter"
    var RSAstocklist: List<Stock> = stocks
    var currentPrices: MutableMap<Long, Pair<Double,String>> = mutableMapOf()

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        lateinit var textView: TextView

        var Row1: TextView
        var Row2: TextView
        var Editbtn : ImageView
        var Delbtn : ImageView

        lateinit var thestock : Stock //Accursed violation of separation of concerns

        init {
            Row1 = v.findViewById(R.id.txtName)
            Row2 = v.findViewById(R.id.txtComment)
            Editbtn = v.findViewById(R.id.imgEditStock)
            Delbtn = v.findViewById(R.id.imgDeleteStock)
            v.setOnClickListener {
                Log.v(TAG, "Element $adapterPosition clicked.")
            }
            Editbtn.setOnClickListener { view ->
                Log.v(TAG, "Edit $adapterPosition clicked.")
                with(view.context) {
                    startActivity<AddEditStockActivity>("EditingExisting" to true,
                            "EditingCrypto" to (thestock.crypto > 0), "TheStock" to thestock)
                }
            }
            Delbtn.setOnClickListener {
                view ->
                Log.v(TAG, "Delete ${adapterPosition} clicked.")
                with(view.context) {
                    alert(resources.getString(R.string.areyousure, adapterPosition)) {
                        positiveButton(R.string.yes) {
                                if (dbFunctions.deletestockInternal(thestock.stockid)) {
                                    toast(R.string.deletesuccess)
                                } else {
                                    toast(R.string.deletefailure)
                                }
                            }
                        negativeButton(R.string.no) { toast(R.string.oknodelete) }
                    }.show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.user_list_row, parent, false)
        return ViewHolder(v)
    }

    // Called by diffResult.dispatchUpdatesTo, Or you can do getChangePayload()
    /*
    override fun onBindViewHolder(holder: ViewHolder?, position: Int, payloads: MutableList<Any>?) {

        if (payloads!!.isEmpty())
            return
        else {
            val o = payloads.get(0) as Bundle
            for (key in o.keySet()) {
                if (key == KEY_TICKER) {
                    //TODO lets update blink discount textView :)
                } else if (key == KEY_PRICE) {
                    //TODO lets update and change price color for some time
                } else if (key == KEY_REVIEWS_COUNT) {
                    //TODO just update the review count textview
                }
            }
        }

        super.onBindViewHolder(holder, position, payloads)
    }
    */

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        Log.d(TAG, "Element $position set.")

        val stock = RSAstocklist[position]
        holder?.Row1?.text = stock.toString()
        holder?.Row2?.text = "Row ${position.toString()} @ " + currentPrices[stock.stockid] ?: "not recently updated"
        holder?.thestock = stock
    }

    fun setCurrentPrice(stockid: Long, price: Double, time: String) {
        currentPrices[stockid] = Pair(price, time)
        val poschanged = RSAstocklist.indexOf(this.RSAstocklist.find{it.stockid == stockid})
        notifyItemChanged(poschanged)
    }

    fun refresh(newitems: List<Stock>) {
        RSAstocklist = newitems

        //Doesn't work, because onBindViewHolder doesn' take the new info, see commented function above
        //But not that important for now since we mash all information together anyway w/ Stock.toString()
        //val diffResult = DiffUtil.calculateDiff(MyDiffCallback(this.RSAstocklist, newitems))
        //diffResult.dispatchUpdatesTo(this)

        notifyDataSetChanged()
    }

    override fun getItemCount() = RSAstocklist.size
}