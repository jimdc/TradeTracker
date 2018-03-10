package com.advent.group69.tradetracker

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.NotificationCompat
import android.support.v7.preference.PreferenceManager

class AlertReceiver : BroadcastReceiver() {

    /**
     * Called when a broadcast is made targeting this class
     */
    override fun onReceive(context: Context, intent: Intent) {
        createNotification(context,
                intent.extras.getString(context.resources.getString(R.string.tickerRoseDroppedMsg)),
                intent.extras.getString(context.resources.getString(R.string.tickerTargetPrice)),
                intent.extras.getString(context.resources.getString(R.string.aboveBelow))
        )
    }

    /* public void GenerateNotify(Context context) {

        NotificationManager myNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(android.R.drawable.ic_btn_speak_now,"hi",100);
        Intent intent=new Intent(context.getApplicationContext(),as.class);
        PendingIntent contentintent=PendingIntent.getBroadcast(context.getApplicationContext(),0, intent, 0);
        notification.setLatestEventInfo(getApplicationContext(), "Hi","date", contentintent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.sound = Uri.parse("android.resource://com.example.serviceproject/" + R.raw.kalimba);
        myNotificationManager.notify(NOTIFICATION_ID,notification);
    } */

    /**
     * Posts a notification to [mNotificationManager] to fire [notificIntent]
     * Settings in [mBuilder] specify: make sound, vibrate, use default light
     * and auto cancel the notification when clicked on in the task bar.
     */
    private fun createNotification(context: Context, msg: String, msgText: String, msgAlert: String) {

        val notificIntent = PendingIntent.getActivity(context, 0,
                Intent(context, MainActivity::class.java), 0)
        val mBuilder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.stocklogo)
                .setContentTitle(msg)
                .setTicker(msgAlert)
                .setContentText(msgText)

        mBuilder.setContentIntent(notificIntent)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val soundPref = sharedPref.getString(context.resources.getString(R.string.notification_sound_key), "new_loud_ringtone.mp3")

        val rawsound = when(soundPref) {
            "home_phone_5.mp3" -> R.raw.home_phone_5
            "new_loud_ringtone.mp3" -> R.raw.new_loud_ringtone
            "old_phone.mp3" -> R.raw.old_phone
            else -> R.raw.new_loud_ringtone
        }

        //mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        val sound = Uri.parse("android.resource://" +
                context.resources.getString(R.string.apppackagename) + "/" + rawsound)

        mBuilder.setSound(sound)
        mBuilder.setDefaults(Notification.DEFAULT_SOUND.inv()) //Don't use default sound
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE)

        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS)
        mBuilder.setAutoCancel(true)

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(1, mBuilder.build())
    }
}