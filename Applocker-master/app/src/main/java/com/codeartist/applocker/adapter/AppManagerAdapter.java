
package com.codeartist.applocker.adapter;

import java.util.ArrayList;

import com.codeartist.applocker.R;
import com.codeartist.applocker.model.AppManagerModel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Wasim on 11/16/16.
 */

public class AppManagerAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<AppManagerModel> mAppList;
    private static LayoutInflater sInflater = null;
    private View.OnClickListener mClickListener;

    public AppManagerAdapter(Context context, View.OnClickListener clickListener) {
        this.mAppList = new ArrayList<>();
        this.mContext = context;
        this.mClickListener = clickListener;
        sInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mAppList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(AppManagerModel item) {
        mAppList.add(item);
    }

    public void addAllItem(ArrayList<AppManagerModel> list) {
        for (AppManagerModel item : list) {
            addItem(item);
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = sInflater.inflate(R.layout.applock_item_app_manager, null);
            holder = new ViewHolder();
            holder.appName = (TextView) view.findViewById(R.id.tvTitle);
            holder.appIcon = (ImageView) view.findViewById(R.id.ivImage);
            holder.locker = (ImageView) view.findViewById(R.id.imageView_locker);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.appIcon.setImageDrawable(mAppList.get(position).getAppIcon());
        holder.appName.setText(mAppList.get(position).getAppName());
        if (mAppList.get(position).isLocked()) {
            holder.locker.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.applock_locker));
        } else {
            holder.locker
                    .setImageDrawable(mContext.getResources().getDrawable(R.mipmap.applock_lock_open));
        }
        holder.locker.setOnClickListener(mClickListener);
        return view;
    }

    private static class ViewHolder {
        public TextView appName;
        public ImageView appIcon, locker;
    }
}
