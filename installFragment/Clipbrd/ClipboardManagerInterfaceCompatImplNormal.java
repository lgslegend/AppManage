package com.lgs.AppManage.AppManage.installFragment.Clipbrd;
/**
 * Created by Lgs on 2016/10/11.
 */


import android.annotation.TargetApi;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;

/**
 * Created by Administrator on 2015/11/25.
 */
//注意这个实际上对应的就是api11 以上的ClipboardManager了，其实这个是最简单的，你只要调用系统的ClipboardManager 即可
//不要遗漏注解 TargetApi 因为遗漏的话 编译会不过的
public class ClipboardManagerInterfaceCompatImplNormal extends ClipboardManagerInterfaceCompatBase {

    ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            notifyPrimaryClipChanged();
        }
    };
    private ClipboardManager mClipboardManager;

    public ClipboardManagerInterfaceCompatImplNormal(Context context) {
        mClipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void addPrimaryClipChangedListener( OnPrimaryClipChangedListener listener) {
        super.addPrimaryClipChangedListener(listener);
        synchronized (mPrimaryClipChangedListeners) {
            if (mPrimaryClipChangedListeners.size() == 1) {
                mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void removePrimaryClipChangedListener(OnPrimaryClipChangedListener listener) {
        super.removePrimaryClipChangedListener(listener);
        synchronized (mPrimaryClipChangedListeners) {
            if (mPrimaryClipChangedListeners.size() == 0) {
                mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public CharSequence getText() {
        return mClipboardManager.getText();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void setText(CharSequence text) {
        if (mClipboardManager != null) {
            mClipboardManager.setText(text);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean hasText() {
        return mClipboardManager != null && mClipboardManager.hasText();
    }

}