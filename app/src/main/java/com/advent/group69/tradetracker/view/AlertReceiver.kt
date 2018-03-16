package com.advent.group69.tradetracker.view

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.NotificationCompat
import android.support.v7.preference.PreferenceManager
import com.advent.group69.tradetracker.viewmodel.MainActivity
import com.advent.group69.tradetracker.R


class AlertReceiver : BroadcastReceiver() {

    /**
     * Called when a broadcast is made targeting this class
     */
    override fun onReceive(context: Context, intent: Intent) {
        createSoundAndOverheadNotification(context,
                intent.extras.getString(context.resources.getString(R.string.tickerRoseDroppedMsg)),
                intent.extras.getString(context.resources.getString(R.string.tickerTargetPrice)),
                intent.extras.getString(context.resources.getString(R.string.aboveBelow))
        )
    }

    private fun createSoundAndOverheadNotification(context: Context, msg: String, msgText: String, msgAlert: String) {

        val notificIntent = PendingIntent.getActivity(context, 0,
                Intent(context, MainActivity::class.java), 0)
        val builder = NotificationCompat.Builder(context, "SoundOverheadChannel")
                .setSmallIcon(R.drawable.stocklogo)
                .setContentTitle(msg)
                .setTicker(msgAlert)
                .setContentText(msgText)

        builder.setContentIntent(notificIntent)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val soundPreference = sharedPreferences.getString(context.resources.getString(R.string.notification_sound_key), "new_loud_ringtone.mp3")

        val rawsound = when(soundPreference) {
            "home_phone_5.mp3" -> R.raw.home_phone_5
            "new_loud_ringtone.mp3" -> R.raw.new_loud_ringtone
            "old_phone.mp3" -> R.raw.old_phone
            else -> R.raw.new_loud_ringtone
        }

        val sound = Uri.parse("android.resource://" +
                context.packageName + "/" + rawsound)
        builder.setSound(sound)
        builder.setDefaults(Notification.DEFAULT_VIBRATE)

        builder.setDefaults(Notification.DEFAULT_LIGHTS)
        builder.setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }
}