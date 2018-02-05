package com.example.group69.alarm

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.group69.alarm.MainActivity

class UserListAdapter(private var activity: Activity, private var items: List<Stock>): BaseAdapter() {

    //var list: List<Stock> = items

    private class ViewHolder(row: View?) {
        var txtName: TextView? = null
        var txtComment: TextView? = null

        init {
            this.txtName = row?.findViewById<TextView>(R.id.txtName)
            this.txtComment = row?.findViewById<TextView>(R.id.txtComment)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.user_list_row, null)
            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        var stock = items[position]
        viewHolder.txtName?.text = stock.toString()
        viewHolder.txtComment?.text = "Click to delete row # " + position.toString()

        return view as View
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

    fun refresh(newitems: List<Stock>) {
        items = newitems
        notifyDataSetChanged()
    }
}