package com.advent.tradetracker.view

import android.app.Activity
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.Context
import android.content.Intent
import android.widget.ImageView
import org.jetbrains.anko.toast
import org.jetbrains.anko.*
import java.util.*
import android.view.MotionEvent
import android.support.design.widget.Snackbar
import com.advent.tradetracker.*
import com.advent.tradetracker.model.AlphabeticalStocks
import com.advent.tradetracker.model.Stock
import com.advent.tradetracker.model.StockInterface
import com.advent.tradetracker.viewmodel.AddEditStockActivity
import com.advent.tradetracker.viewmodel.EDIT_SOMETHING
import com.advent.tradetracker.viewmodel.MainActivity
import timber.log.Timber



/**
 * Provide views to RecyclerView with data from stockList.
 */

class RecyclingStockAdapter(
        stocks: List<Stock>,
        private var dragStartListener: OnStartDragListener,
        private var context: Context?)
    : ItemTouchHelperAdapter, RecyclerView.Adapter<RecyclingStockAdapter.ItemViewHolder>() {

    private var stockList: MutableList<Stock> = stocks.toMutableList()
    private var deletedStocks = Stack<Stock>()
    private var currentPrices: MutableMap<Long, Pair<Double,String>> = mutableMapOf()
    private var callBackMainActivity: StockInterface? = null

    init {
        try {
            if (context is MainActivity) {
                this.callBackMainActivity = context as StockInterface
            }
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("Activity must implement StockInterface")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater
                .from(parent.context)
                .inflate(R.layout.stock_list_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        Timber.d("Element $position set.")

        val stock = stockList[position]
        if (stock.crypto == 1L) {
            //holder.itemView?.setBackgroundColor(Color.GRAY)
            //will uncomment once the color bug is fixed (other stocks keep randomly flickering and turning gray)
        }

        val stockOrCrypto = if (stock.crypto == 0L) "Stock" else "Crypto"
        val targetOperator = if (stock.above == 1L) ">" else "<"

        holder.rowStockAndAlarm?.text = context?.resources?.getString(R.string.rowStockAndAlarm,
                stockOrCrypto,
                stock.ticker,
                targetOperator,
                dollarFormatter(stock.target)
        )
        val currentPrice = currentPrices[stock.stockid]?.toString() ?: context?.resources?.getString(R.string.notUpdatedRecently)
        holder.rowCurrentPrice?.text = context?.resources?.getString(R.string.rowCurrentPrice, position, currentPrice)
        //To be passed for "edit" function
        holder.thestock = stock

        //Start a drag whenever hte handle view is touched
     /*   holder.itemView.onTouch {
            _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    dragStartListener.onStartDrag(holder)
                }
            false
        } */
    }

    override fun onItemDismiss(view: RecyclerView.ViewHolder, position: Int) {

        Timber.v("Delete $position clicked.")
        if (view is ItemViewHolder) {
            if (callBackMainActivity?.deleteStockByStockId(view.thestock.stockid) == true) {
                deletedStocks.push(view.thestock)
                val mySnackbar = Snackbar.make(view.itemView,
                        view.itemView.context.resources.getString(R.string.deletesuccess),
                        Snackbar.LENGTH_SHORT)
                mySnackbar.setAction(R.string.undo_string, MyUndoListener())
                mySnackbar.show()
            } else {
                view.itemView.context.toast(R.string.deletefailure)
            }
        } else {
            view.itemView.context.toast("Internal error: ViewHolder passed was not ItemViewHolder. Delete this stock through edit.")
        }
        refresh(stockList)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(stockList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }
    /**
     * This will take a double and remove the trailing zeros except for 1 zero if hundreds place is not used.
     */
    fun dollarFormatter(num: Double): String {
        var s : String = num.toString()

        var ss : String =  s.trimEnd('0' )
        if(ss.endsWith('.')){
            return ss.plus("00")
        }
        else if(ss.indexOf('.') == ss.length - 2){
            //if i knew how to do regular expressions i'd check for a period(.) followed by a single character. I improvised
            return ss.plus("0")
        }
        else {
            return ss
        }
    }
    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     */
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ItemTouchHelperViewHolder {

        var rowStockAndAlarm: TextView? = itemView.findViewById(R.id.txtName)
        var rowCurrentPrice: TextView? = itemView.findViewById(R.id.txtComment)
        private var btnEdit = itemView.findViewById<ImageView>(R.id.imgEditStock)
        lateinit var thestock : Stock //Accursed violation of separation of concerns

        init {
            btnEdit.setOnClickListener { view ->
                Timber.v("Edit $adapterPosition clicked.")
                if (!::thestock.isInitialized) {
                    Timber.d("thestock is not initialized; I can't pass to AddEditActivity!")
                }

                val intent = Intent(view.context as Activity, AddEditStockActivity::class.java)
                intent.putExtra("isEditingCrypto", thestock.crypto > 0L)
                        .putExtra("isEditingExisting", true)
                        .putExtra("TheStock", thestock)

                val activity = view.context as Activity
                activity.startActivityForResult(intent, EDIT_SOMETHING)
            }
        }

        override fun onItemSelected() {
            //itemView.setBackgroundColor(Color.DKGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    inner class MyUndoListener : View.OnClickListener {
        override fun onClick(v: View) {
            with(v.context) {
                if (deletedStocks.isEmpty()) toast("Cannot restore stock; deletedStocks list is empty")
                else {
                    val stockToRestore = deletedStocks.pop()
                    if (callBackMainActivity?.addOrEditStock(stockToRestore) == true) {
                        toast("Successfully restored stock ${stockToRestore.ticker}")
                    } else {
                        toast("Could not re-add stock ${stockToRestore.ticker}")
                    }
                }
            }
        }
    }


    fun setCurrentPrice(stockid: Long, price: Double, time: String) {
        currentPrices[stockid] = Pair(price, time)
        val positionChanged = stockList.indexOf(this.stockList.find{it.stockid == stockid})
        notifyItemChanged(positionChanged)
    }

    fun refresh(newStockList: List<Stock>) {

        if (!stockList.containsAll(newStockList) || stockList.size != newStockList.size) { //To preserve order
            Timber.v( "refresh called; I'm changing something")
            stockList = newStockList.sortedWith(AlphabeticalStocks).toMutableList()
            notifyDataSetChanged()
        } else {
            Timber.v( "refresh called BUT doesn't meet my criteria")
        }

        //Doesn't work, because onBindViewHolder doesn' take the new info, see commented function above
        //But not that important for now since we mash all information together anyway w/ Stock.toString()
        //val diffResult = DiffUtil.calculateDiff(MyDiffCallback(this.stockList, newStockList))
        //diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = stockList.size
}