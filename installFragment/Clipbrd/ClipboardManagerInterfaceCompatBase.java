package com.lgs.AppManage.AppManage.installFragment.Clipbrd;
/**
 * Created by Lgs on 2016/10/11.
 */


import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/25.
 */
//既然我们是要对api11 以上和以下 分别做2个 实体类出来，而且这2个实体类 都必须实现我们的自定义接口。
//所以不妨先定义一个base 的抽象类
public  abstract class ClipboardManagerInterfaceCompatBase implements ClipboardManagerInterfaceCompat {

    //这个抽象类实际上就只做了一件事 维持一个监听器的list 罢了。
    //注意OnPrimaryClipChangedListener 这个类 是我们自定义的，不是高于api11的源码里的
    protected final ArrayList<OnPrimaryClipChangedListener> mPrimaryClipChangedListeners
            = new ArrayList<OnPrimaryClipChangedListener>();

    @Override
    public void addPrimaryClipChangedListener(OnPrimaryClipChangedListener listener) {
        synchronized (mPrimaryClipChangedListeners) {
            mPrimaryClipChangedListeners.add(listener);
        }
    }

    //这个方法其实还挺重要的 就是通知所有在这个上面的listenser 内容发生了变化
    //注意这里的mPrimaryClipChangedListeners是自定义的 不是系统的
    protected final void notifyPrimaryClipChanged() {
        synchronized (mPrimaryClipChangedListeners) {
            for (int i = 0; i < mPrimaryClipChangedListeners.size(); i++) {
                mPrimaryClipChangedListeners.get(i).onPrimaryClipChanged();
            }
        }
    }

    @Override
    public void removePrimaryClipChangedListener(OnPrimaryClipChangedListener listener) {
        synchronized (mPrimaryClipChangedListeners) {
            mPrimaryClipChangedListeners.remove(listener);
        }
    }
}