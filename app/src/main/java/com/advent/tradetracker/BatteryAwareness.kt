package com.advent.tradetracker

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import com.advent.tradetracker.BatteryAwareness.isPowerSavingOn
import com.advent.tradetracker.BatteryAwareness.notifiedOfPowerSaving
import com.advent.tradetracker.BatteryAwareness.wentThroughFirstTimeFalseAlarm
import com.advent.tradetracker.view.MorePowerInfoNotification
import org.jetbrains.anko.powerManager
import org.jetbrains.anko.toast
import timber.log.Timber

object BatteryAwareness {

    var notifiedOfPowerSaving = false
    var isPowerSavingOn = false
    var wentThroughFirstTimeFalseAlarm = false

    private const val POWER_OFF_PLEASE_ID = 33
    private const val NOTIFY_INTENT_FILTER = "POWERSAVENOTIFY"
    private const val CHANGE_INTENT_FILTER = "android.os.action.POWER_SAVE_MODE_CHANGED" // For API version 21 and above

    private val powerSaverOffPleaseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                NOTIFY_INTENT_FILTER -> checkPowerSaverMode(context)
                CHANGE_INTENT_FILTER -> checkPowerSaverMode(context)
            }
        }
    }

    fun registerReceiver(localBroadcastManager: LocalBroadcastManager, powerManager: PowerManager) {
        val filter = IntentFilter()
        filter.addAction(BatteryAwareness.CHANGE_INTENT_FILTER)
        filter.addAction(BatteryAwareness.NOTIFY_INTENT_FILTER)
        localBroadcastManager.registerReceiver(BatteryAwareness.powerSaverOffPleaseReceiver, filter)

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(powerManager.isPowerSaveMode)
                isPowerSavingOn = true
        }
    }

    fun unregisterReceiver(localBroadcastManager: LocalBroadcastManager) {
        localBroadcastManager.unregisterReceiver(powerSaverOffPleaseReceiver)
    }

    fun checkPowerStatusAndNotify(localBroadcastManager: LocalBroadcastManager) {
        if(isPowerSavingOn && !notifiedOfPowerSaving) {
            val intent = Intent(BatteryAwareness.NOTIFY_INTENT_FILTER)
            localBroadcastManager.sendBroadcast(intent)
        }
    }

    private fun checkPowerSaverMode(context: Context?) {
        if (isPowerSavingOn) {
            if (wentThroughFirstTimeFalseAlarm) {
                showDisablePowerSavingRequestNotification(context)
                context?.toast("Turn off power saving mode so scan can run while phone is sleeping")
                notifiedOfPowerSaving = true
            } else {
                wentThroughFirstTimeFalseAlarm = true
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

    private fun showDisablePowerSavingRequestNotification(context: Context?) {
        if (context != null) {
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
        } else {
            Timber.d("Context is null; could not show disable power saving request notification.")
        }
    }

    class WakeLocker(powerManager: PowerManager) {

        private val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag")

        fun wakeLockDo(timeout: Long, importantFunction: () -> Unit) {
            wakeLock.acquire(timeout)

            importantFunction()

            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}