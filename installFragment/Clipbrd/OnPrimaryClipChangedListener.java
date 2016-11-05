package com.lgs.AppManage.AppManage.installFragment.Clipbrd;

/**
 * Created by Lgs on 2016/10/11.
 */
//注意这个OnPrimaryClipChangedListener 是在api11以后才有的
//我们这里就是把这个接口给拿出来 定义一下 看下CliboardManager的源码就知道了(注意要看api11 以后的源码)
public interface OnPrimaryClipChangedListener {
    void onPrimaryClipChanged();
}