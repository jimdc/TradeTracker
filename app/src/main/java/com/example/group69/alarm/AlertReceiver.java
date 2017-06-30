package com.example.group69.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver{

    // Called when a broadcast is made targeting this class
    @Override
    public void onReceive(Context context, Intent intent) {

        createNotification(context,
                context.getResources().getString(R.string.timesup),
                context.getResources().getString(R.string.fivepass),
                context.getResources().getString(R.string.alert)
        );

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
    public void createNotification(Context context, String msg, String msgText, String msgAlert){

        // Define an Intent and an action to perform with it by another application
        PendingIntent notificIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        // Builds a notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ntt_logo_24_24)
                        .setContentTitle(msg)
                        .setTicker(msgAlert)
                        .setContentText(msgText);

        // Defines the Intent to fire when the notification is clicked
        mBuilder.setContentIntent(notificIntent);

        // Set the default notification option
        // DEFAULT_SOUND : Make sound
        // DEFAULT_VIBRATE : Vibrate
        // DEFAULT_LIGHTS : Use the default light notification
        //mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        Uri sound = Uri.parse("android.resource://" + "com.example.group69.alarm" + "/" + R.raw.new_loud_ringtone);
        mBuilder.setSound(sound);
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
        // Auto cancels the notification when clicked on in the task bar
        mBuilder.setAutoCancel(true);
      //  Notification note = mBuilder.build();

        // Gets a NotificationManager which is used to notify the user of the background event
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Post the notification
        mNotificationManager.notify(1, mBuilder.build());

    }
}