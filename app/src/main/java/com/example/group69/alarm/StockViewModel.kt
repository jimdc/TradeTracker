package com.example.group69.alarm

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData


class StockViewModel : ViewModel() {
    var stocks: MutableLiveData<List<Stock>>
    //var stokos: LiveData<List<Stoko>>

    init {
        stocks = MutableLiveData<List<Stock>>()

        //stokos = dbService.getStocklistFromDB()
    }
}
