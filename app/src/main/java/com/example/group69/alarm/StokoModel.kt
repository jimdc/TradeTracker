package com.example.group69.alarm

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class StokoModel {
    fun addStoko(ticker: String) {
        val stoko = Stoko(ticker)

        Single.fromCallable {
            Alarm.database?.stokoDao()?.insertStoko(stoko)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    /*fun registerAllStokoListener() {
        Alarm.database?.stokoDao()?.getAllStokos()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { listOfStokos ->
                    //view.stokoTableUpdated(listOfStokos)
                }
    }*/
}