package com.example.group69.alarm

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData


class StockViewModel : ViewModel() {
    var stocks: MutableLiveData<List<Stock>>

    init { stocks = MutableLiveData<List<Stock>>() }
}
