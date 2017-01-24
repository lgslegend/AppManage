package com.codeartist.applocker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.codeartist.applocker.db.DBManager;
import com.codeartist.applocker.utility.Utils;

/**
 * Created by bjit-16 on 12/29/16.
 */

public class PackageRemoveReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getEncodedSchemeSpecificPart();
        DBManager mDb = DBManager.getInstance(context);
        Utils.deleteFromAppLockerTable(packageName, mDb);
        Log.e("package", packageName+" try to remove ");
    }
}
