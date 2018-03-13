package com.advent.group69.tradetracker

import android.content.ContentValues.TAG
import android.graphics.Color
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
import java.util.*
import android.view.MotionEvent
import android.support.v4.view.MotionEventCompat

/**
 * Provide views to RecyclerView with data from RSAstocklist.
 */
class RecyclingStockAdapter(stocks: List<Stock>, var mDragStartListener: OnStartDragListener)
    : ItemTouchHelperAdapter, RecyclerView.Adapter<RecyclingStockAdapter.ItemViewHolder>() {

    val TAG = "RecyclingStockAdapter"
    var RSAstocklist: MutableList<Stock> = stocks.toMutableList()
    var currentPrices: MutableMap<Long, Pair<Double,String>> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.stock_list_row, parent, false)
        return ItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        Log.d(TAG, "Element $position set.")

        val stock = RSAstocklist[position]
        if (stock.crypto == 1L) {
            holder?.itemView?.setBackgroundColor(Color.GRAY);
        }

        val sType = if (stock.crypto == 0L) "Stock" else "Crypto"
        val sOperator = if (stock.above == 1L) ">" else "<"
        holder.Row1.text = "$sType: alert if ${stock.ticker} $sOperator ${stock.target}"

        val currprice = currentPrices[stock.stockid]
        holder.Row2.text = "Row ${position.toString()} @ ${if (currprice != null) currprice.toString() else " not updated recently"}"

        //To be passed for "edit" function
        holder.thestock = stock

        //Start a drag whenever hte handle view is touched
        holder.itemView.onTouch {
            view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder)
                }
            false
        }
    }

    override fun onItemDismiss(position: Int) {
        RSAstocklist.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(RSAstocklist, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     */
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ItemTouchHelperViewHolder {

        val handleView = itemView.findViewById<ImageView>(R.id.handle)
        lateinit var textView: TextView

        var Row1: TextView
        var Row2: TextView
        var Editbtn : ImageView
        var Delbtn : ImageView

        lateinit var thestock : Stock //Accursed violation of separation of concerns

        init {
            Row1 = itemView.findViewById(R.id.txtName)
            Row2 = itemView.findViewById(R.id.txtComment)
            Editbtn = itemView.findViewById(R.id.imgEditStock)
            Delbtn = itemView.findViewById(R.id.imgDeleteStock)
            itemView.setOnClickListener {
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
        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.DKGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }


    fun setCurrentPrice(stockid: Long, price: Double, time: String) {
        currentPrices[stockid] = Pair(price, time)
        val poschanged = RSAstocklist.indexOf(this.RSAstocklist.find{it.stockid == stockid})
        notifyItemChanged(poschanged)
    }

    fun refresh(newitems: List<Stock>) {
        RSAstocklist = newitems.toMutableList()

        //Doesn't work, because onBindViewHolder doesn' take the new info, see commented function above
        //But not that important for now since we mash all information together anyway w/ Stock.toString()
        //val diffResult = DiffUtil.calculateDiff(MyDiffCallback(this.RSAstocklist, newitems))
        //diffResult.dispatchUpdatesTo(this)

        notifyDataSetChanged()
    }

    override fun getItemCount() = RSAstocklist.size
}