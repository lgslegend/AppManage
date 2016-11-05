/*
package com.lgs.AppManage.AppManage.BackupR;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;

import com.lgs.AppManage.AppManage.BackupR.dialogs.BatchConfirmDialog;
import com.lgs.AppManage.R;

import java.io.File;
import java.util.ArrayList;

public class BatchActivity extends BaseActivity
        implements OnClickListener, BatchConfirmDialog.ConfirmListener {
    ArrayList<AppInfo> appInfoList = OAndBackup.appInfoList;
    final static String TAG = OAndBackup.TAG;
    boolean backupBoolean = true;
    final static int SHOW_DIALOG = 0;
    final static int CHANGE_DIALOG = 1;
    final static int DISMISS_DIALOG = 2;

    final static int RESULT_OK = 0;

    boolean checkboxSelectAllBoolean = false;
    boolean changesMade;

    File backupDir;
    PowerManager powerManager;
    SharedPreferences prefs;

    BatchAdapter adapter;
    ArrayList<AppInfo> list;

    RadioButton rbData, rbApk, rbBoth;

    HandleMessages handleMessages;
    ShellCommands shellCommands;
    Sorter sorter;

    long threadId = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testlayout);

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        handleMessages = new HandleMessages(this);

        if (savedInstanceState != null) {
            threadId = savedInstanceState.getLong("threadId");
            Utils.reShowMessage(handleMessages, threadId);
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backupDirPath = prefs.getString("pathBackupFolder", FileCreationHelper.getDefaultBackupDirPath());
        backupDir = Utils.createBackupDir(BatchActivity.this, backupDirPath);

        int filteringMethodId = 0;
        int sortingMethodId = 0;
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            backupBoolean = extra.getBoolean("dk.jens.backup.backupBoolean");
            //   filteringMethodId = extra.getInt("dk.jens.backup.filteringMethodId");
            // sortingMethodId = extra.getInt("dk.jens.backup.sortingMethodId");
        }
        ArrayList<String> users = getIntent().getStringArrayListExtra("dk.jens.backup.users");
        shellCommands = new ShellCommands(prefs, users);

        Button bt = (Button) findViewById(R.id.backupRestoreButton);
        bt.setOnClickListener(this);
        rbApk = (RadioButton) findViewById(R.id.radioApk);
        rbData = (RadioButton) findViewById(R.id.radioData);
        rbBoth = (RadioButton) findViewById(R.id.radioBoth);
        rbBoth.setChecked(true);

        if (appInfoList == null)
          //  appInfoList = AppInfoHelper.listTranAppinfo( new PackageUtil(this).querySysApp(),this);//AppInfoHelper.getPackageInfo(this, backupDir, true);


        if (backupBoolean) {
            list = new ArrayList<AppInfo>();
            for (AppInfo appInfo : appInfoList)
                if (appInfo.isInstalled())
                    list.add(appInfo);

            bt.setText(R.string.backup);
        } else {
            list = new ArrayList<AppInfo>(appInfoList);
            bt.setText(R.string.restore);
        }

        ListView listView = (ListView) findViewById(R.id.listview);
        adapter = new BatchAdapter(this, R.layout.batchlistlayout, list);
        // sorter = new Sorter(adapter, prefs);
        // sorter.sort(filteringMethodId);
        // sorter.sort(sortingMethodId);
        listView.setAdapter(adapter);
        // onItemClickListener gør at hele viewet kan klikkes - med onCheckedListener er det kun checkboxen der kan klikkes
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                AppInfo appInfo = adapter.getItem(pos);
                appInfo.setChecked(!appInfo.isChecked());
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void finish() {
        setResult(RESULT_OK, constructResultIntent());
        super.finish();
    }

    @Override
    public void onDestroy() {
        if (handleMessages != null) {
            handleMessages.endMessage();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("threadId", threadId);
    }

    @Override
    public void onClick(View v) {
        final ArrayList<AppInfo> selectedList = new ArrayList<AppInfo>();
        for (AppInfo appInfo : list) {
            if (appInfo.isChecked()) {
                selectedList.add(appInfo);
             }
        }

        String message = "";
        for (AppInfo appInfo : selectedList)
            message = message + appInfo.getLabel() + "\n";
        new com.lgs.AppManage.AppManage.BackupR.dialogs.AlertDialog(this).builder().setTitle("back").setMsg(message).setCancelable(false)
                .setPositiveButton("确认", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //BatchConfirmDialog.ConfirmListener activity =   BatchConfirmDialog.ConfirmListener.this ;
                        onConfirmed(selectedList);
                    }
                }).setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();

    }

    public Intent constructResultIntent() {
        Intent result = new Intent();
        result.putExtra("changesMade", changesMade);
        // result.putExtra("filteringMethodId", sorter.getFilteringMethod().getId());
        //  result.putExtra("sortingMethodId", sorter.getSortingMethod().getId());
        return result;
    }

    @Override
    public void onConfirmed(ArrayList<AppInfo> selectedList) {
        final ArrayList<AppInfo> list = new ArrayList<AppInfo>(selectedList);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                doAction(list);
            }
        });
        thread.start();
        threadId = thread.getId();
    }

    public void doAction(ArrayList<AppInfo> selectedList) {
        if (backupDir != null) {
            Crypto crypto = null;
            if (backupBoolean && prefs.getBoolean("enableCrypto", false) && Crypto.isAvailable(this))
                crypto = getCrypto();
            PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            if (prefs.getBoolean("acquireWakelock", true)) {
                wl.acquire();
                Log.i(TAG, "wakelock acquired");
            }
            changesMade = true;
            int id = (int) System.currentTimeMillis();
            int total = selectedList.size();
            int i = 1;
            boolean errorFlag = false;
            for (AppInfo appInfo : selectedList) {
                // crypto may be needed for restoring even if the preference is set to false
                if (!backupBoolean && appInfo.getLogInfo() != null && appInfo.getLogInfo().isEncrypted() && Crypto.isAvailable(this))
                    crypto = getCrypto();
                if (appInfo.isChecked()) {
                    String message = "(" + Integer.toString(i) + "/" + Integer.toString(total) + ")";
                    String title = backupBoolean ? getString(R.string.backupProgress) : getString(R.string.restoreProgress);
                    title = title + " (" + i + "/" + total + ")";
                    NotificationHelper.showNotification(BatchActivity.this, BatchActivity.class, id, title, appInfo.getLabel(), false);
                    handleMessages.setMessage(appInfo.getLabel(), message);
                    int mode = AppInfo.MODE_BOTH;
                    if (rbApk.isChecked())
                        mode = AppInfo.MODE_APK;
                    else if (rbData.isChecked())
                        mode = AppInfo.MODE_DATA;
                    if (backupBoolean) {
                        if (BackupRestoreHelper.backup(this, backupDir, appInfo, shellCommands, mode) != 0)
                            errorFlag = true;
                        else if (crypto != null) {
                            crypto.encryptFromAppInfo(this, backupDir, appInfo, mode, prefs);
                            if (crypto.isErrorSet()) {
                                Crypto.cleanUpEncryptedFiles(new File(backupDir, appInfo.getPackageName()), appInfo.getSourceDir(), appInfo.getDataDir(), mode, prefs.getBoolean("backupExternalFiles", false));
                                errorFlag = true;
                            }
                        }
                    } else {
                        if (BackupRestoreHelper.restore(this, backupDir, appInfo, shellCommands, mode, crypto) != 0)
                            errorFlag = true;
                    }
                    if (i == total) {
                        String msg = backupBoolean ? getString(R.string.batchbackup) : getString(R.string.batchrestore);
                        String notificationTitle = errorFlag ? getString(R.string.batchFailure) : getString(R.string.batchSuccess);
                        NotificationHelper.showNotification(BatchActivity.this, BatchActivity.class, id, notificationTitle, msg, true);
                        handleMessages.endMessage();
                    }
                    i++;
                }
            }
            if (wl.isHeld()) {
                wl.release();
                Log.i(TAG, "wakelock released");
            }
            if (errorFlag) {
                Utils.showErrors(BatchActivity.this);
            }
        }
    }
}
*/
