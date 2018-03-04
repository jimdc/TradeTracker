package com.example.group69.alarm

import android.support.v7.util.DiffUtil

/**
 * Substitute for costly notifySetDataChanged
 * see https://medium.com/@iammert/using-diffutil-in-android-recyclerview-bdca8e4fbb00
 * Not used yet.
 */

class MyDiffCallback(internal var newStocks: List<Stock>, internal var oldStocks: List<Stock>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldStocks.size
    }

    override fun getNewListSize(): Int {
        return newStocks.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldStocks[oldItemPosition].stockid === newStocks[newItemPosition].stockid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldStocks[oldItemPosition].equals(newStocks[newItemPosition])
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}