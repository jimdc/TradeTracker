package com.example.group69.alarm

import android.support.v7.util.DiffUtil
import android.os.Bundle

/**
 * Not used yet. It' a substitute for costly notifySetDataChanged
 * see https://medium.com/@iammert/using-diffutil-in-android-recyclerview-bdca8e4fbb00
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
/*
    public final val KEY_TICKER = "ticker"
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val newStock = newStocks.get(newItemPosition)
        val oldStock = oldStocks.get(oldItemPosition)
        val diffBundle = Bundle()

        if (newStock.ticker != oldStock.ticker) {
            diffBundle.putString(KEY_TICKER, newStock.ticker)
        }

        if (newStock.hasDiscount() !== oldProduct.hasDiscount()) {
            diffBundle.putBoolean(KEY_DISCOUNT, newProduct.hasDiscount())
        }
        if (newProduct.getReviews().size() !== oldProduct.getReviews().size()) {
            diffBundle.putInt(Product.KEY_REVIEWS_COUNT, newProduct.getReviews().size())
        }
        if (newProduct.getPrice() !== oldProduct.getPrice()) {
            diffBundle.putFloat(Product.KEY_PRICE, newProduct.getPrice())
        }
        return if (diffBundle.size() == 0) null else diffBundle
    }
*/
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}