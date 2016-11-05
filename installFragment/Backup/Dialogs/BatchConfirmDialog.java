package com.lgs.AppManage.AppManage.installFragment.Backup.Dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import com.lgs.AppManage.AppManage.installFragment.Backup.AppInfo;
import com.lgs.AppManage.AppManage.installFragment.Backup.OAndBackup;
import com.lgs.AppManage.R;

import java.util.ArrayList;

public class BatchConfirmDialog extends DialogFragment
{
    final static String TAG = OAndBackup.TAG;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        final ArrayList<AppInfo> selectedList = arguments.getParcelableArrayList("selectedList");
        boolean backupBoolean = arguments.getBoolean("backupBoolean");
        String title = backupBoolean ? getString(R.string.backupConfirmation) : getString(R.string.restoreConfirmation);
        String message = "";
        for(AppInfo appInfo : selectedList)
            message = message + appInfo.getLabel() + "\n";
        new  AlertDialog(getActivity()).builder().setTitle(message)
                .setPositiveButton("确认退出", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ConfirmListener activity = (ConfirmListener) getActivity();
                        activity.onConfirmed(selectedList);
                    }
                }).setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }


    public interface ConfirmListener
    {
        void onConfirmed(ArrayList<AppInfo> selectedList);
    }
}
