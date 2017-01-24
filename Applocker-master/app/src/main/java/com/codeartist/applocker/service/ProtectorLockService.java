package com.codeartist.applocker.service;

import com.codeartist.applocker.utility.Constants;
import com.codeartist.applocker.utility.Utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by bjit-16 on 1/4/17.
 */

public class ProtectorLockService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        startForeground(Constants.FOREGROUND_NOTIFICATION_ID, Utils.createNotification(this));
        sendBroadcast(new Intent(Constants.PROTECTOR_COMPLETE_BROADCAST_KEY));
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

}
