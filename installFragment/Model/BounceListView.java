package com.lgs.AppManage.AppManage.installFragment.Model;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;



public class BounceListView extends ListView {

    private float maxBounceAxisYScroll = 20;
    public BounceListView(Context context) {
        super(context);
    }
    public BounceListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public BounceListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, (int)maxBounceAxisYScroll, isTouchEvent);
    }
}