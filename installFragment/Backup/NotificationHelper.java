package com.lgs.AppManage.AppManage.installFragment.Backup;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.lgs.AppManage.R;

public class NotificationHelper
{
    public static void showNotification(Context context, Class c, int id, String title, String text, boolean autocancel)
    {
        if(!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("disableNotifications", false))
        {
    //        Notification.Builder mBuilder = new Notification.Builder(this)
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(autocancel);
            Intent resultIntent = new Intent(context, c);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                // make sure messages aren't overdrawn - taskstackbuilder doesn't seem to work well with single_top
            /*
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(c);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            */
            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

         //   mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(id, mBuilder.build());
        }
    }
}