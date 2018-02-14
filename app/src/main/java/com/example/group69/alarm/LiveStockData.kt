/*package com.example.group69.alarm

import android.databinding.BaseObservable
import android.databinding.Bindable

import java.util.Observable

/**
 * Data binding will be implemented later. Too frustrating now
 */
class LiveStockData(private var stockid: Long, private var currPrice: Double) : BaseObservable() {

    @Bindable
    fun getstockid(): Long {
        return this.stockid
    }

    @Bindable
    fun getcurrPrice() : Double {
        return this.currPrice
    }

    fun setstockid(newstockid: Long) {
        this.stockid = newstockid
        //notifyPropertyChanged(BR.stockid)
    }

    fun setcurrPrice(newcurrPrice: Double) {
        this.currPrice = newcurrPrice
        //notifyPropertyChanged(BR.currPrice)
    }
}*/