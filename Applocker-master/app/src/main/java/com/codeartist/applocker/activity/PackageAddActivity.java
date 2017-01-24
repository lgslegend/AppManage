
package com.codeartist.applocker.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.codeartist.applocker.R;
import com.codeartist.applocker.db.DBManager;
import com.codeartist.applocker.utility.Constants;
import com.codeartist.applocker.utility.Utils;

import java.util.List;

/**
 * Created by bjit-16 on 1/4/17.
 */

public class PackageAddActivity extends AppCompatActivity {
    AlertDialog.Builder dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String packageName = getIntent().getStringExtra(Constants.KEY_PKG_NAME);
        DBManager mDb = DBManager.getInstance(this);
        showDialogForLock(mDb, this, packageName);
    }

    private void showDialogForLock(final DBManager db, Context context, final String packageName) {

        if (dialog == null) {
            dialog = new AlertDialog.Builder(context);
        } else {
            return;
        }

        dialog.setTitle(context.getString(R.string.app_name_lockapp))
                .setIcon(R.mipmap.ic_launcher_android)
                .setMessage(context.getString(R.string.lock_request_message,
                        getAppNameFromPackage(packageName, context)))
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                        finish();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        Utils.insertInToAppLockerTable(packageName, db);
                        dialoginterface.cancel();
                        finish();
                    }
                }).show();

    }

    private String getAppNameFromPackage(String packageName, Context context) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppsList = context.getPackageManager()
                .queryIntentActivities(mainIntent, 0);
        for (ResolveInfo app : pkgAppsList) {
            if (app.activityInfo.packageName.equals(packageName)) {
                return app.activityInfo.loadLabel(context.getPackageManager()).toString();
            }
        }
        return null;
    }
}
