package com.lgs.AppManage.AppManage.installFragment.View;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lgs.AppManage.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author vincent
 * 自定义ListView回弹，
 * 实现添加自定义布局动画效果
 * 在listView的上方
 */
public class RefreshListView extends ListView implements OnScrollListener {

    private static final String TAG = RefreshListView.class.getSimpleName();

    public static final int STATE_PULL_DOWN_REFRESH = 0;// 下拉刷新
    public static final int STATE_RELEASE_REFRESH = 1;// 释放刷新
    public static final int STATE_REFRESHING = 2;// 下拉刷新中

    public static final int STATE_DRAG_UP_REFRESH = 3;// 上拉刷新
    public static final int STATE_DRAG_UP_RELEASE_REFRESH = 4;// 上拉释放刷新
    public static final int STATE_DRAG_UP_REFRESHING = 5;// 上拉刷新中

    private int mCurrentPullDownState = STATE_PULL_DOWN_REFRESH; // 下拉刷新默认状态
    private int mCurrentDragUpState = STATE_DRAG_UP_REFRESH;      // 上拉加载默认状态

    //头布局界面主要控件
    private LinearLayout mHeaderLayout;
    private ImageView ivArrow;
    private ProgressBar mProgressBar;
    private TextView tvState;
    private TextView tvLastUpdateTime;
    private int mHeaderLayoutHeight;// 下拉刷新view的高度

    //头布局动画效果
    private Animation upAnimation;        // 向上旋转的动画
    private Animation downAnimation;  // 向下旋转的动画

    //尾布局
    private LinearLayout mFooterLayout;
    private int mFooterLayoutHeight;
    private TextView mFooterText; // 上拉状态提示

    private boolean isLoadingMore    = false;    // 是否正在加载更多中
    //    private boolean isScrollToBottom = false;    // 是否滑动到底部
    private boolean isTouching = false;    // listView是否触摸状态
    private boolean onceTouch = true;

    //    private int firstVisibleItemPosition;        // 第一可见条目的位置
    private int scrollState = OnScrollListener.SCROLL_STATE_IDLE;//初始的滚动状态

    private float mDownY;        // actionDown的Y值
    private float hiddenTop;  // 底布局需隐藏的位置

//    private View mCustomHeaderView;        // 自定义添加的headview

    private OnRefreshListener mOnRefershListener;    // 刷新时的回调监听

    private static final int MAX_Y_OVERSCROLL_DISTANCE = 200;
    private static final float SCROLL_RATIO = 0.5f;// 阻尼系数
    private Context mContext;
    private int mMaxYOverscrollDistance;

    public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initBounceListView();//添加阻尼效果

        initHeaderLayout();// 加载头布局
        initFooterLayout(); // 加载尾布局
        //设置条目点击监听
        this.setOnItemClickListener(new InterceptOnItemClickListener());
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshListView(Context context) {
        this(context, null);
    }

//    /* 预留给以后拓展 */
//    public void addCustomHeaderView(View headerView) {
//        this.mCustomHeaderView = headerView;
//        mHeaderLayout.addView(headerView);
//    }

    /**
     * 添加阻尼效果
     */
    private void initBounceListView() {
        final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        final float density = metrics.density;
        mMaxYOverscrollDistance = (int) (density * MAX_Y_OVERSCROLL_DISTANCE);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int newDeltaY = deltaY;
        int delta = (int) (deltaY * SCROLL_RATIO);
        if (delta != 0) newDeltaY = delta;
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, mMaxYOverscrollDistance, isTouchEvent);
    }

    /**
     * 下拉加载头布局初始化，开始时界面隐藏
     */
    private void initHeaderLayout() {

        // 加载头布局以及头布局控件
        mHeaderLayout = (LinearLayout) View.inflate(getContext(), R.layout.refreshlist__listview_header, null);
        ivArrow = (ImageView) mHeaderLayout.findViewById(R.id.iv_listview_header_arrow);
        mProgressBar = (ProgressBar) mHeaderLayout.findViewById(R.id.pb_listview_header);
        tvState = (TextView) mHeaderLayout.findViewById(R.id.tv_listview_header_state);
        tvLastUpdateTime = (TextView) mHeaderLayout.findViewById(R.id.tv_listview_header_last_update_time);

        // 设置最后刷新时间
        tvLastUpdateTime.setText(mContext.getString(R.string.latest_update) + getCurrentTimeString());
        // 系统会帮我们测量出headerView的高度
        mHeaderLayout.measure(0, 0);
        mHeaderLayoutHeight = mHeaderLayout.getMeasuredHeight();
        Log.i(TAG, "测量后的高度: " + mHeaderLayoutHeight);
        // 设置初始显示位置
        mHeaderLayout.setPadding(0, -mHeaderLayoutHeight, 0, 0);
        // 添加headerView
        this.addHeaderView(mHeaderLayout);
        initAnimation();
    }

    /**
     * 上拉刷新尾布局初始化，开始界面隐藏
     */
    private void initFooterLayout() {
        // 加载尾布局
        mFooterLayout = (LinearLayout) View.inflate(getContext(), R.layout.refreshlist_listview_footer, null);
        mFooterText = (TextView) mFooterLayout.findViewById(R.id.tv_footer_text);

        // 测量出footerView的高度
        mFooterLayout.measure(0, 0);
        mFooterLayoutHeight = mFooterLayout.getMeasuredHeight();
        Log.i(TAG, "脚布局的高度: " + mFooterLayoutHeight);
        // 设置布局隐藏
        mFooterLayout.setPadding(0, -mFooterLayoutHeight, 0, 0);
        // 添加FooterView
        this.addFooterView(mFooterLayout);
        // 设置当listView滑动时的监听
        this.setOnScrollListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN:
                isTouching = false;
                mDownY = ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:

                float moveY = ev.getY();

                int diffY = (int) (moveY - mDownY);    //获得touch移动距离
                if (Math.abs(diffY) > 3) {
                    isTouching = true;    //touching状态
                }

                // 如果当前的状态是正在刷新 父类不响应touch事件 (避免拉出侧滑菜单)
                if (mCurrentPullDownState == STATE_REFRESHING) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                }

                // 当前状态是上拉正在加载
                if (mCurrentDragUpState == STATE_DRAG_UP_REFRESHING) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                }

                // diffY < 0:上拉 diffY > 0:下拉
                // 当前显示的位置是第一个 并且向下滑动 为 下拉刷新
                if (getFirstVisiblePosition() == 0 && diffY > 0 && (scrollState == OnScrollListener.SCROLL_STATE_IDLE)) {

                    //设置下拉头布局的逐步显示
                    getParent().requestDisallowInterceptTouchEvent(true);
                    // 给头布局设置paddingTop, 设置需要显示的高度
                    int hiddenHeight = (int) (mHeaderLayoutHeight - diffY + 0.5f);
                    mHeaderLayout.setPadding(0, -hiddenHeight, 0, 0);

                    // diffY < mRefreshViewHeight : 头布局未完全显示，切换为下拉刷新状态
                    if (diffY < mHeaderLayoutHeight && mCurrentPullDownState == STATE_RELEASE_REFRESH) {
                        mCurrentPullDownState = STATE_PULL_DOWN_REFRESH;    // 更新状态
                        refreshUIByState(mCurrentPullDownState);//更新为下拉刷新状态显示

                    } else if (diffY >= mHeaderLayoutHeight && mCurrentPullDownState == STATE_PULL_DOWN_REFRESH) {
                        // diffY > mRefreshViewHeight : 释放刷新
                        mCurrentPullDownState = STATE_RELEASE_REFRESH;    // 更新状态
                        refreshUIByState(mCurrentPullDownState);//更新为释放刷新状态显示
                    }
                    // 需要自己响应touch
                    return true;
                }

                //当前显示界面已到达最后
                if ((getLastVisiblePosition() == getAdapter().getCount() - 1) && diffY < 0) {
                    if (onceTouch) {
                        hiddenTop = ev.getY() + 0.5f;
                        onceTouch = false;
                    }
                    diffY = (int) (hiddenTop - ev.getY() + 0.5f);
                    // 当前显示的位置是最后一个并且是向上滑动 为上拉 加载
                    // Log.d(TAG, "mCurrentDragUpState"+mCurrentDragUpState);
                    // 给底布局设置paddingBottom
                    int hiddenHeight = (int) (mFooterLayoutHeight - diffY + 0.5f);
                    mFooterLayout.setPadding(0, 0, 0, -hiddenHeight);

                    if (hiddenHeight > 0 && mCurrentDragUpState == STATE_DRAG_UP_RELEASE_REFRESH) {
                        // 更新状态
                        mCurrentDragUpState = STATE_DRAG_UP_REFRESH;
                        refreshUIByState(mCurrentDragUpState);// UI 更新
                        Log.d(TAG, "上拉加载更多...");
                    } else if (hiddenHeight <= 0 && mCurrentDragUpState == STATE_DRAG_UP_REFRESH) {
                        // 更新状态
                        mCurrentDragUpState = STATE_DRAG_UP_RELEASE_REFRESH;
                        refreshUIByState(mCurrentDragUpState);// UI 更新
                        Log.d(TAG, "松开加载更多...");
                    }
                    // 需要自己响应touch 不能返回true
                    // return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDownY = 0;
                onceTouch = true;
                // 释放后的操作
                if (mCurrentPullDownState == STATE_PULL_DOWN_REFRESH) {
                    // 如果是 下拉刷新状态，直接缩回去
                    mHeaderLayout.setPadding(0, -mHeaderLayoutHeight, 0, 0);
                } else if (mCurrentPullDownState == STATE_RELEASE_REFRESH) {
                    // 如果是释放刷新状态，用户希望去 刷新数据--》正在刷新状态
                    mCurrentPullDownState = STATE_REFRESHING;
                    // 设置paddingTop 为0
                    mHeaderLayout.setPadding(0, 0, 0, 0);
                    refreshUIByState(mCurrentPullDownState);// UI更新
                    // 通知 调用者 现在处于 正在刷新状态
                    if (mOnRefershListener != null) {
                        mOnRefershListener.onRefreshing();
                    }
                }

                // 处理上拉加载
                if (mCurrentDragUpState == STATE_DRAG_UP_REFRESH) {
                    // 如果是 上拉刷新状态，直接缩回去
                    mFooterLayout.setPadding(0, 0, 0, -mFooterLayoutHeight);
                } else if (mCurrentDragUpState == STATE_DRAG_UP_RELEASE_REFRESH) {
                    // 如果是释放刷新状态，用户希望去 刷新数据--》正在刷新状态
                    mCurrentDragUpState = STATE_DRAG_UP_REFRESHING;

                    // 设置paddingTop 为0
                    mFooterLayout.setPadding(0, 0, 0, 0);
                    isLoadingMore = true;
                    refreshUIByState(mCurrentDragUpState);// UI更新

                    // 通知 调用者 现在处于 正在刷新状态
                    if (mOnRefershListener != null) {
                        mOnRefershListener.onLoadingMore();
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 根据当前动画状态显示动画
     *
     * @param CurrentState
     */
    private void refreshUIByState(int CurrentState) {
        switch (CurrentState) {

            case STATE_PULL_DOWN_REFRESH:// 下拉刷新
                tvState.setText(mContext.getString(R.string.pull_down_refresh));
                ivArrow.startAnimation(downAnimation);    // 执行向下旋转
                break;

            case STATE_RELEASE_REFRESH:// 松开刷新
                tvState.setText(mContext.getString(R.string.refresh_after_release));
                ivArrow.startAnimation(upAnimation);    // 执行向上旋转
                break;

            case STATE_REFRESHING:// 正在刷新
                ivArrow.clearAnimation();
                ivArrow.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                tvState.setText(mContext.getString(R.string.refreshing_now));
                break;

            case STATE_DRAG_UP_REFRESH:// 上拉刷新
                mFooterText.setText(mContext.getString(R.string.pull_up_load_more_data));
                break;

            case STATE_DRAG_UP_RELEASE_REFRESH:// 松开刷新
                mFooterText.setText(mContext.getString(R.string.refresh_after_release));
                break;

            case STATE_DRAG_UP_REFRESHING:// 正在刷新
                mFooterText.setText(mContext.getString(R.string.refreshing_now));
                break;
        }
    }

    /**
     * 告知 ListView刷新完成
     */
    public void refreshFinish() {
        if (isLoadingMore) {
            // 上拉加载
            mFooterLayout.setPadding(0, 0, 0, -mFooterLayoutHeight);
            mCurrentDragUpState = STATE_DRAG_UP_REFRESH;
            refreshUIByState(mCurrentDragUpState);
            isLoadingMore = false;
        } else {
            // 隐藏 刷新的View
            mHeaderLayout.setPadding(0, -mHeaderLayoutHeight, 0, 0);
            // 状态重置
            mCurrentPullDownState = STATE_PULL_DOWN_REFRESH;
            // UI更新
            refreshUIByState(mCurrentPullDownState);
        }
    }

    /**
     * listView滚动时的调用方法, 用于判断是否滑动到底部
     * firstVisibleItem    当前屏幕显示在顶部的item的position
     * visibleItemCount 当前屏幕显示了多少个条目的总数.
     * totalItemCount	   ListView的总条目的总数
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
    }

    /**
     * 当滚动状态改变时回调
     * SCROLL_STATE_IDLE  					停滞状态
     * SCROLL_STATE_TOUCH_SCROLL	按住时滚动的状态
     * SCROLL_STATE_FLING 					猛地一滑
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    /**
     * 设置刷新的监听事件
     *
     * @param listener
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mOnRefershListener = listener;
    }

    /**
     * 滑动事件回调接口
     */
    public interface OnRefreshListener {

        /**
         * 正在刷新时的回调
         */
        void onRefreshing();

        /**
         * 加载更多, 加载完成后需要把脚布局隐藏
         */
        void onLoadingMore();

        /**
         * 处理item点击事件
         */
        void implOnItemClickListener(AdapterView<?> parent, View view, int position, long id);
    }

    /**
     * 用于实现条目点击事件的监听
     */
    public class InterceptOnItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if (isTouching || mCurrentPullDownState == STATE_REFRESHING || mCurrentDragUpState == STATE_DRAG_UP_REFRESHING) {
                return;
            }
            mOnRefershListener.implOnItemClickListener(parent, view, position, id);
        }
    }

    /**
     * 初始化动画效果
     */
    private void initAnimation() {
        upAnimation = new RotateAnimation(0f, -180f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        upAnimation.setDuration(500);
        // this animation performed will persist when it is finished
        upAnimation.setFillAfter(true);

        downAnimation = new RotateAnimation(-180f, -360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        downAnimation.setDuration(500);
        downAnimation.setFillAfter(true);    // 动画结束后, 停留在结束的位置上
    }

    /**
     * 获得当前时间的字符串形式
     */
    private String getCurrentTimeString() {
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(time));
    }
}