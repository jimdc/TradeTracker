package com.example.group69.alarm

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.content.BroadcastReceiver
import android.content.Intent
import com.example.group69.alarm.MainActivity

/**
 * Converts a list of Stocks to a list of Views for the ListView container
 * @return a BaseAdapter that may be loaded into a ListView
 */
class UserListAdapter(private var activity: Activity, private var items: List<Stock>): BaseAdapter() {
    var currentPrices: MutableMap<Long, Double> = mutableMapOf()

    /**
     * @constructor Find the listview resources for internal class use
     */
    private class ViewHolder(row: View?) {
        var txtName: TextView? = null
        var txtComment: TextView? = null

        init {
            this.txtName = row?.findViewById<TextView>(R.id.txtName)
            this.txtComment = row?.findViewById<TextView>(R.id.txtComment)
        }
    }

    /**
     * Display the correct data on the view according to the position
     * @return a View with its txtName and txtComment set
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            //val binding = DataBindingUtil.inflate(inflater, R.layout.user_list_row, null, false) as UserListRowBinding

            view = inflater.inflate(R.layout.user_list_row, null)

            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        var stock = items[position]
        viewHolder.txtName?.text = stock.toString()
        viewHolder.txtComment?.text = "Click to delete row # " + position.toString() + " @" + currentPrices[stock.stockid]

        return view as View
    }

    fun setCurrentPrice(stockid: Long, price: Double) {
        currentPrices[stockid] = price
        notifyDataSetChanged()
    }

    override fun getItem(i: Int): Stock {
        return items[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }

    /**
     * Synchronize's the adapter's stocklist with the parameter
     * @param[newitems] The new stocklist reference
     */
    fun refresh(newitems: List<Stock>) {
        items = newitems
        notifyDataSetChanged()
    }
}