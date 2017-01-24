
package com.codeartist.applocker.activity;

import java.util.ArrayList;
import java.util.List;

import com.codeartist.applocker.R;
import com.codeartist.applocker.adapter.AppManagerAdapter;
import com.codeartist.applocker.db.DBManager;
import com.codeartist.applocker.model.AppManagerModel;
import com.codeartist.applocker.service.AppLockerService;
import com.codeartist.applocker.utility.Constants;
import com.codeartist.applocker.utility.Preferences;
import com.codeartist.applocker.utility.Utils;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

/**
 * Created by Wasim on 11/16/16.
 */

public final class AppManagerActivity extends BaseServiceBinderActivity {
    // private Button mAllAppsButton, mLockAppsButton, mPaidAppsButton;
    private ListView mAppListView;
    private List<AppManagerModel> mAppList;
    private ProgressBar mProgressBar;
    private AppManagerAdapter mAdapter;
    private static final String SCHEME = "package";
    private DBManager mDb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applock_activity_app_manager);
        /*
         * mAllAppsButton = (Button) findViewById(R.id.button_allApp); mLockAppsButton = (Button)
         * findViewById(R.id.button_lockApp); mPaidAppsButton = (Button)
         * findViewById(R.id.button_paidApp);
         */
        mAppListView = (ListView) findViewById(R.id.listView_appManager);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar_AppList);
        mAdapter = new AppManagerAdapter(this, mClickListener);
        mAppListView.setAdapter(mAdapter);
        if (!Utils.isMyServiceRunning(AppLockerService.class, this)) {
            startService(new Intent(this, AppLockerService.class));
        }
        mDb = DBManager.getInstance(getApplicationContext());
        mAppListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                final AppManagerModel item = (AppManagerModel) adapterView
                        .getItemAtPosition(position);

                showInstalledAppDetails(item.getPackageName());

            }
        });
        bindService(new Intent(this, AppLockerService.class), mConnection,
                BIND_AUTO_CREATE);
        new GetApplication().execute();
        // addShortcut();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (!isPermittedToLollipop()) {
                alertForUserAccess();
            }
        }



        // sendMessageToService(getPackageName(), AppLockerService.MSG_APP_LOCK_SCREEN);
        registerReceiver(activityClose, new IntentFilter("closeActivity"));
        registerReceiver(closeRecent, new IntentFilter("closeRecent"));

    }

    /*
     * private boolean isMyServiceRunning(Class<?> serviceClass, Context context) { ActivityManager
     * manager = (ActivityManager) context .getSystemService(Context.ACTIVITY_SERVICE); for
     * (ActivityManager.RunningServiceInfo service : manager .getRunningServices(Integer.MAX_VALUE))
     * { if (serviceClass.getName().equals(service.service.getClassName())) { return true; } }
     * return false; }
     */

    private BroadcastReceiver activityClose = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private BroadcastReceiver closeRecent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startActivity(new Intent(AppManagerActivity.this, DummyActivity.class));
            finish();
        }
    };

    public final static int REQUEST_CODE_OVERLAY = 101; /* (see edit II) */
    public final static int REQUEST_CODE_ACCESS = 202; /* (see edit II) */

    public void checkDrawOverlayPermission() {
        /** check if we already have permission to draw over other apps */
        if (!isEnableToDrawOverlays()) {
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            /** request permission via start activity for result */
            startActivityForResult(intent, REQUEST_CODE_OVERLAY);
        }
    }

    private void alertForUserAccess(){
        AlertDialog.Builder  dialog = new AlertDialog.Builder(this);
        dialog.setTitle("应用权限")
                .setIcon(R.mipmap.ic_launcher_android)
                .setMessage("需要应用统计权限")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), REQUEST_CODE_ACCESS);
                        dialoginterface.cancel();
                    }
                }).show();

    }

    private void alertForOverlays() {
        AlertDialog.Builder  dialog = new AlertDialog.Builder(this);
        dialog.setTitle("悬浮窗权限")
                .setIcon(R.mipmap.ic_launcher_android)
                .setMessage("需要悬浮窗权限")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        checkDrawOverlayPermission();
                        dialoginterface.cancel();
                    }
                }).show();

    }

    private boolean isEnableToDrawOverlays(){
       return Settings.canDrawOverlays(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean isPermittedToLollipop() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(),
                    0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void clockWiseOrAntiClockWise(final ImageView image, final boolean isLocked) {
        int anim = isLocked ? R.anim.applock_rotate_clock_wise : R.anim.applock_rotate_anti_clock_wise;
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                anim);
        image.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isLocked) {
                    image.setImageDrawable(getResources().getDrawable(R.mipmap.applock_locker));
                } else {
                    image.setImageDrawable(getResources().getDrawable(R.mipmap.applock_lock_open));
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void appLocked(View view, String packageName) throws ClassCastException {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if(!isPermittedToLollipop()){
                alertForUserAccess();
                return;
            } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(!isEnableToDrawOverlays()){
                    alertForOverlays();
                    return;
                }
            }
        }

        if (view != null) {
            ImageView image = (ImageView) view;
            clockWiseOrAntiClockWise(image, true);
        }
        sendMessageToService(packageName, AppLockerService.MSG_APP_LOCK);
        // Log.e("applock_locker", "locked");
    }

    private void appUnlocked(View view, String packageName) throws ClassCastException {
        if (view != null) {
            ImageView image = (ImageView) view;
            clockWiseOrAntiClockWise(image, false);
        }
        sendMessageToService(packageName, AppLockerService.MSG_APP_UNLOCK);
        // Log.e("applock_locker", "unlocked");
    }

    /*
     * @Override public void onWindowFocusChanged(boolean hasFocus) {
     * super.onWindowFocusChanged(hasFocus); Log.d("Focus debug", "Focus changed !"); if(!hasFocus)
     * { Log.d("Focus debug", "Lost focus !"); Intent closeDialog = new
     * Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS); sendBroadcast(closeDialog); } }
     */


    private void addShortcut() {
        // Adding shortcut for MainActivity
        // on Home screen
        Intent shortcutIntent = new Intent(getApplicationContext(),
                AppManagerActivity.class);

        shortcutIntent.setAction(Intent.ACTION_MAIN);

        Intent addIntent = new Intent();
        addIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                        R.mipmap.ic_launcher_android));

        addIntent
                .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        addIntent.putExtra("duplicate", false); // may it's already there so don't duplicate
        getApplicationContext().sendBroadcast(addIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.applock_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * @Override public void sendMessageToService(String packageName, int lockFlag) { //
     * Log.e("applock_locker", mBound + ""); if (!mBound) return; // Create and send a message to the
     * service, using a supported 'what' value Message msg = Message.obtain(null, lockFlag, 0, 0);
     * Bundle bundle = new Bundle(); bundle.putString(Constants.KEY_PKG_NAME, packageName);
     * msg.setData(bundle); try { mService.send(msg); } catch (RemoteException e) {
     * e.printStackTrace(); } }
     */

    static final int REQUEST_CODE = 120;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Log.e("onActivity Result", resultCode + " " + requestCode);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            String packageName = data.getStringExtra(Constants.KEY_PKG_NAME);
            if (packageName != null) {
                for (int i = 0; i < mAppList.size(); i++) {
                    if (mAppList.get(i).getPackageName().equals(packageName)) {
                        mAppList.get(i).setLocked(true);
                        mAdapter.notifyDataSetChanged();
                        appLocked(null, packageName);
                    }
                }
            }
        } else if(requestCode == REQUEST_CODE_ACCESS){
            if(isPermittedToLollipop()){
                Log.e("lollipop", "get permission");
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alertForOverlays();
                }
            }else{
                Log.e("lollipop", "not get permission");
            }

        }else if (requestCode == REQUEST_CODE_OVERLAY) {
            if(isEnableToDrawOverlays()){
                Log.e("lollipop upper", "get permission draw overlays");
            }else{
                Log.e("lollipop upper", "not get permission draw overlays");
            }
        }
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View parentRow = (View) v.getParent();
            ListView listView = (ListView) parentRow.getParent();
            final int position = listView.getPositionForView(parentRow);
            AppManagerModel item = mAppList.get(position);
            String password = Preferences.loadString(getApplicationContext(),
                    Preferences.KEY_APP_LOCKER_PASSWORD, null);
            if (password == null) {
                Intent intent;
                int lockType = Preferences.loadInt(AppManagerActivity.this,
                        Constants.KEY_LOCKER_TYPE, Constants.PATTERN_LOCK);
                if (lockType == Constants.PATTERN_LOCK) {
                    intent = new Intent(AppManagerActivity.this, PatternSetterActivity.class);

                } else {
                    intent = new Intent(AppManagerActivity.this, PasswordSetterActivity.class);
                }

                // Intent intent = new Intent(AppManagerActivity.this, PatternSetterActivity.class);
                intent.putExtra(Constants.KEY_PKG_NAME, item.getPackageName());
                startActivityForResult(intent, REQUEST_CODE);
                return;
            }
            if (item.isLocked()) {
                mAppList.get(position).setLocked(false);
                appUnlocked(v, item.getPackageName());
            } else {
                mAppList.get(position).setLocked(true);
                appLocked(v, item.getPackageName());
            }
        }
    };

    private class GetApplication extends AsyncTask<Void, String, ArrayList<AppManagerModel>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<AppManagerModel> doInBackground(Void... voids) {
            return getInstalledApps();
        }

        @Override
        protected void onPostExecute(ArrayList<AppManagerModel> list) {
            super.onPostExecute(list);
            mAppList = list;
            if (getWindow().getDecorView() != null) {
                mAdapter.addAllItem(list);
                mProgressBar.setVisibility(View.GONE);
            }
        }
    }

    public void showInstalledAppDetails(String packageName) {
        try{ Intent intent = new Intent();
        // final int apiLevel = Build.VERSION.SDK_INT;
       intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts(SCHEME, packageName, null);
        intent.setData(uri);
	   startActivity(intent);}catch(Exception e){}
    }

    public ArrayList<AppManagerModel> getInstalledApps() {
        ArrayList<AppManagerModel> applicationList = new ArrayList<>();

        /*
         * List<ApplicationInfo> packages = getPackageManager()
         * .getInstalledApplications(PackageManager.GET_META_DATA);
         */
        ArrayList<String> lockedApp = Utils.getLockedApp(mDb);
        AppManagerModel install = new AppManagerModel();
        install.setAppName("应用包安装程序");
        install.setAppIcon(getResources().getDrawable(R.mipmap.ic_launcher_android));
        install.setPackageName("com.android.packageinstaller");
        if (lockedApp.contains("com.android.packageinstaller")) {
            install.setLocked(true);
        } else {
            install.setLocked(false);
        }
        applicationList.add(install);

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppsList = getPackageManager().queryIntentActivities(mainIntent, 0);
        for (ResolveInfo info : pkgAppsList) {
            Log.e("apps installed", info.activityInfo.packageName + "");
            AppManagerModel appManagerModel = new AppManagerModel();
            appManagerModel.setAppName(info.activityInfo.loadLabel(getPackageManager()).toString());
            appManagerModel.setAppIcon(info.activityInfo.loadIcon(getPackageManager()));
            appManagerModel.setPackageName(info.activityInfo.packageName);
            if (lockedApp.contains(info.activityInfo.packageName)) {
                appManagerModel.setLocked(true);
            } else {
                appManagerModel.setLocked(false);
            }

            applicationList.add(appManagerModel);

        }

        /*
         * for (ApplicationInfo appInfo : packages) { if ((appInfo.flags &
         * ApplicationInfo.FLAG_SYSTEM) == 0) { // // Third Party Applications AppManagerModel
         * appManagerModel = new AppManagerModel();
         * appManagerModel.setAppName(appInfo.loadLabel(getPackageManager()).toString());
         * appManagerModel.setAppIcon(appInfo.loadIcon(getPackageManager()));
         * appManagerModel.setPackageName(appInfo.packageName); if
         * (lockedApp.contains(appInfo.packageName)) { appManagerModel.setLocked(true); } else {
         * appManagerModel.setLocked(false); } applicationList.add(appManagerModel); } }
         */ return applicationList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        unregisterReceiver(activityClose);
        unregisterReceiver(closeRecent);
    }
}
