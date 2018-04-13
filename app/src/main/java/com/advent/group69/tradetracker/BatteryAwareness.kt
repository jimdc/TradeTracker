package com.advent.group69.tradetracker

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.advent.group69.tradetracker.view.MorePowerInfoNotification
import org.jetbrains.anko.toast

object BatteryAwareness {

    var notifiedOfPowerSaving = false
    var isPowerSavingOn = false
    var wentThroughFirstTimeFalseAlarm = false

    private const val POWER_OFF_PLEASE_ID = 33
    const val INTENT_FILTER = "POWERSAVENOTIFY"

    val powerSaverOffPleaseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                INTENT_FILTER -> if (isPowerSavingOn) {
                    if (wentThroughFirstTimeFalseAlarm) {
                        BatteryAwareness.showDisablePowerSavingRequestNotification(context!!)
                        context.toast("Turn off power saving mode so scan can run while phone is sleeping")
                        notifiedOfPowerSaving = true
                    } else {
                        wentThroughFirstTimeFalseAlarm = true
                    }
                }
            }
        }
    }

    /*
    fun isPhonePluggedIn(context: Context): Boolean {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val extraStatus = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isBatteryCharging = (extraStatus == BatteryManager.BATTERY_STATUS_CHARGING)

        val plugStatus = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val isChargingByUsb = (plugStatus == BatteryManager.BATTERY_PLUGGED_USB)
        val isChargingByAc = (plugStatus == BatteryManager.BATTERY_PLUGGED_AC)

        return (isBatteryCharging or isChargingByUsb or isChargingByAc)
    }
    */

    fun showDisablePowerSavingRequestNotification(context: Context) {

        val notificBuilder = NotificationCompat.Builder(context, "DisablePowerChannel")
                .setContentTitle("please disable power saving mode to keep scanning while phone screen is off")
                .setContentText("CLICK THIS NOTIFICATION for more information")
                .setTicker("C")
                .setSmallIcon(R.drawable.stocklogo)

        val moreInfoIntent = Intent(context, MorePowerInfoNotification::class.java)
        val taskStackBuilder = TaskStackBuilder.create(context)

        taskStackBuilder.addParentStack(MorePowerInfoNotification::class.java)
        taskStackBuilder.addNextIntent(moreInfoIntent)

        val pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        notificBuilder.setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(POWER_OFF_PLEASE_ID, notificBuilder.build())
    }
}