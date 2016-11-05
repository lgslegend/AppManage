package com.lgs.AppManage.AppManage.installFragment.Backup.Dialogs;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;

import com.lgs.AppManage.AppManage.installFragment.Backup.OAndBackup;
import com.lgs.AppManage.R;

public class CreateDirectoryDialog extends DialogFragment
{
    final static String TAG = OAndBackup.TAG;

    EditText editText;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle arguments = getArguments();
        final String root = arguments.getString("root");
       Builder builder = new  Builder(getActivity());
        builder.setTitle(R.string.filebrowser_createDirectory);
        builder.setMessage(R.string.filebrowser_createDirectoryDlgMsg);

        editText = new EditText(getActivity());
        if(savedInstanceState != null)
            editText.setText(savedInstanceState.getString("edittext"));
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(lp);
        builder.setView(editText);

        builder.setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                String dirname = editText.getText().toString();
                try
                {
                    PathListener activity = (PathListener) getActivity();
                    activity.onPathSet(root, dirname);
                }
                catch(ClassCastException e)
                {
                    Log.e(TAG, "CreateDirectoryDialog: " + e.toString());
                }
            }
        });
        builder.setNegativeButton(R.string.dialogCancel, null);
        return builder.create();
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString("edittext", editText.getText().toString());
    }
    public interface PathListener
    {
        void onPathSet(String root, String dirname);
    }
}
