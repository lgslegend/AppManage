package com.lgs.AppManage.AppManage.installFragment.Clipbrd;


/**
 * Created by Lgs on 2016/10/11.
 */
//这里我们就定义一个接口，这个接口囊括了 所有我们需要使用的方法
//注意后三个方法 api11以下也是有的，而前2个方法 11或者11以上才有
public interface ClipboardManagerInterfaceCompat {

    //注意这里的参数 我们使用的是自己定义的接口 而不是sdk里面的ClipboardManager.OnPrimaryClipChangedListener
    void addPrimaryClipChangedListener(  OnPrimaryClipChangedListener listener);

    void removePrimaryClipChangedListener( OnPrimaryClipChangedListener listener);

    CharSequence getText();

    void setText(CharSequence text);

    boolean hasText();


}