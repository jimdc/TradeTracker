package com.example.group69.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.NotificationCompat

class AlertReceiver : BroadcastReceiver() {

    // Called when a broadcast is made targeting this class
    override fun onReceive(context: Context, intent: Intent) {
        //Log.d("alarmo","ayy")
        //Log.d("alarmo",intent.extras.getString("message1"))
        createNotification(context,
                intent.extras.getString("message1").toString(),
                intent.extras.getString("message2").toString(),
                intent.extras.getString("message3").toString()
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
    private fun createNotification(context: Context, msg: String, msgText: String, msgAlert: String) {

        // Define an Intent and an action to perform with it by another application
        val notificIntent = PendingIntent.getActivity(context, 0,
                Intent(context, MainActivity::class.java), 0)
        // Builds a notification
        val mBuilder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.stocklogo)
                .setContentTitle(msg)
                .setTicker(msgAlert)
                .setContentText(msgText)

        // Defines the Intent to fire when the notification is clicked
        mBuilder.setContentIntent(notificIntent)

        // Set the default notification option
        // DEFAULT_SOUND : Make sound
        // DEFAULT_VIBRATE : Vibrate
        // DEFAULT_LIGHTS : Use the default light notification
        //mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        val sound = Uri.parse("android.resource://" + "com.example.group69.alarm" + "/" + R.raw.new_loud_ringtone)
        mBuilder.setSound(sound)
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE)
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS)
        // Auto cancels the notification when clicked on in the task bar
        mBuilder.setAutoCancel(true)
        //  Notification note = mBuilder.build();

        // Gets a NotificationManager which is used to notify the user of the background event
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Post the notification
        mNotificationManager.notify(1, mBuilder.build())

        //val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        //val num : LongArray = longArrayOf(1500,1500,1500)
        //v.vibrate(num,1)
        //Thread.sleep(17000)
        //v.cancel()

    }
}