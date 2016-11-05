package com.lgs.AppManage.AppManage.Clipbrd;

/**
 * Created by Lgs on 2016/10/11.
 */


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.ClipboardManager;
import android.text.TextUtils;

import com.lgs.AppManage.AppManage.installFragment.Clipbrd.ClipboardManagerInterfaceCompatBase;
import com.lgs.AppManage.AppManage.installFragment.Clipbrd.OnPrimaryClipChangedListener;

/**
 * Created by Administrator on 2015/11/25.
 */
//这个就是对应的api11 以下的ClipboardManager 实体类了，实际上这里主要就是要实现api11 以上的那个监听
//我们就用一个最简单的方法 不断监视text变化就可以了
//思路其实也挺简单的 就是把这个 ClipboardManagerInterfaceCompatImplCustom
public class ClipboardManagerInterfaceCompatImplCustom extends ClipboardManagerInterfaceCompatBase implements Runnable {

    //静态的不会导致内存泄露
    private static Handler mHandler;
    private CharSequence mLastText;
    //这个是设置间隔多少毫秒去检查一次 默认我们设置成1000ms检查一次
    public static int CHECK_TIME_INTERVAL = 1000;


    static {
        mHandler = new Handler(Looper.getMainLooper());
    }

    //api11 以下 是android.text.ClipboardManager; 注意和api11以上的android.content.ClipboardManager是 有区别的
    ClipboardManager clipboardManager;

    public ClipboardManagerInterfaceCompatImplCustom(Context context) {
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }


    @Override
    public void addPrimaryClipChangedListener(OnPrimaryClipChangedListener listener) {
        super.addPrimaryClipChangedListener(listener);
        synchronized (mPrimaryClipChangedListeners) {
            if (mPrimaryClipChangedListeners.size() == 1) {
                startListenDataChange();
            }
        }
    }

    @Override
    public void removePrimaryClipChangedListener(OnPrimaryClipChangedListener listener) {
        super.removePrimaryClipChangedListener(listener);
        synchronized (mPrimaryClipChangedListeners) {
            if (mPrimaryClipChangedListeners.size() == 0) {
                stopListenDataChange();
            }
        }
    }

    private void stopListenDataChange() {
        mHandler.removeCallbacks(this);
    }

    private void startListenDataChange() {
        mLastText = getText();
        mHandler.post(this);
    }


    @Override
    public CharSequence getText() {

        if (clipboardManager == null) {
            return null;
        }

        return clipboardManager.getText();
    }

    @Override
    public void setText(CharSequence text) {
        if (clipboardManager != null) {
            clipboardManager.setText(text);
        }
    }

    @Override
    public boolean hasText() {
        if (clipboardManager==null)
        {
            return false;
        }
        return clipboardManager.hasText();
    }

    @Override
    public void run() {

        CharSequence data=getText();
        isChanged(data);
        mHandler.postDelayed(this,CHECK_TIME_INTERVAL);

    }

    private void isChanged(CharSequence data)
    {
        if (TextUtils.isEmpty(mLastText) && TextUtils.isEmpty(data)) {
            return;
        }
        if (!TextUtils.isEmpty(mLastText) && data != null && mLastText.toString().equals(data.toString())) {
            return;
        }
        mLastText = data;
        //如果发生了变化 就通知
        notifyPrimaryClipChanged();
    }
}