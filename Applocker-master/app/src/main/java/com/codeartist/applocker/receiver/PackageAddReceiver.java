
package com.codeartist.applocker.receiver;

import com.codeartist.applocker.activity.PackageAddActivity;
import com.codeartist.applocker.utility.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by bjit-16 on 1/4/17.
 */

public class PackageAddReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("package Installed", "received");
        String packageName = intent.getData().getEncodedSchemeSpecificPart();
        Intent i = new Intent(context, PackageAddActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(Constants.KEY_PKG_NAME, packageName);
        context.startActivity(i);

    }

}
