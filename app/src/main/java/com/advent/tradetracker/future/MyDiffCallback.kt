package com.advent.tradetracker.future

import android.support.v7.util.DiffUtil
import com.advent.tradetracker.model.Stock

/**
 * Not used yet. It' a substitute for costly notifySetDataChanged
 * see https://medium.com/@iammert/using-diffutil-in-android-recyclerview-bdca8e4fbb00
 */

@SuppressWarnings("unused")
class MyDiffCallback(private var newStocks: List<Stock>, private var oldStocks: List<Stock>) : DiffUtil.Callback() {

    override fun getOldListSize() = oldStocks.size
    override fun getNewListSize() = newStocks.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldStocks[oldItemPosition].stockid == newStocks[newItemPosition].stockid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldStocks[oldItemPosition] == newStocks[newItemPosition]
    }
}