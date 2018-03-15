package com.advent.group69.tradetracker

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

@SuppressWarnings("unused")
class PowerManagement {
    fun isPhonePluggedIn(context: Context): Boolean {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val extraStatus = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isBatteryCharging = (extraStatus == BatteryManager.BATTERY_STATUS_CHARGING)

        val plugStatus = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val isChargingByUsb = (plugStatus == BatteryManager.BATTERY_PLUGGED_USB)
        val isChargingByAc = (plugStatus == BatteryManager.BATTERY_PLUGGED_AC)

        return (isBatteryCharging or isChargingByUsb or isChargingByAc)
    }
}