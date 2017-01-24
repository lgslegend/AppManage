
package com.codeartist.applocker.utility;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.codeartist.applocker.R;
import com.codeartist.applocker.db.DBManager;
import com.codeartist.applocker.service.AppLockerService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public final class Utils {
    public static final int APP_INFO_FLAG = 0;
    public static final Object APP_DEFAULT_NAME_STRING = "No App Name";
    private static final long ADD_24_HOURS_IN_MILISEC = 24 * 60 * 60 * 1000;

    public static String convertTime(long inputTime, String datePattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
        Date time = new Date(inputTime);
        return dateFormat.format(time);
    }


    // Need to call this method with expiration date string, after receiving response from serial
    // validation request
    public static void saveExpirationTime(String strDate, Context context) {
        // YYYYMMDD HHMMSS
        if (strDate == null || strDate == "") {
            return;
        }
        String year = strDate.substring(0, 4);
        String month = strDate.substring(4, 6);
        String day = strDate.substring(6, 8);
        String hour = "23";
        String min = "59";
        String sec = "59";

        // yyyy/MM/dd HH:mm:ss
        strDate = year + "/" + month + "/" + day + " " + hour + ":" + min + ":" + sec;
        long time = getTimeFromString(strDate);
        // save the time on preference, so that alarm can be reschedule after reboot
        Preferences.save(context, Preferences.KEY_EXPIRATION_TIME_LONG, time);
        // scheduleExpirationChecker(context, time);
    }

    public static long getTimeFromString(String strDate) {
        SimpleDateFormat f = new SimpleDateFormat(Constants.RESPONSE_TIME_FORMAT);
        Date d = null;
        try {
            d = f.parse(strDate);
            return d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getApiLevel() {
        return Build.VERSION.SDK_INT;
    }


    public static int getColor(String s) {
        if (s.contains("CLEAN")) {
            return Color.BLACK;
        }
        if (s.contains("INFECTED")) {
            return Color.RED;
        }
        if (s.contains("ADWARE")) {
            return Color.YELLOW;
        }
        return Color.BLUE; // in case of ERROR
    }



    public static void hideSoftKeyboard(Activity activity) {
        try {
            if (activity.getCurrentFocus() != null) {
                InputMethodManager imm = (InputMethodManager) activity
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        DateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }



    private static long getPkgIdFromDB(String pkgName, DBManager db) {
        long pkgId;
        String query = "select " + DBManager.Pkg.COL_ID + " FROM " + DBManager.Pkg.TABLE + " WHERE "
                + DBManager.Pkg.COL_PACKAGE + " ='" + pkgName + "'";
        Cursor cursor = db.doRawQuery(query);

        if (cursor == null || !cursor.moveToFirst()) {
            pkgId = insertInToPkgTable(pkgName, db);
        } else {
            pkgId = cursor.getLong(0);
        }

        if (cursor != null) {
            cursor.close();
        }

        return pkgId;
    }

    private static long insertInToPkgTable(String pkgName, DBManager db) {
        ContentValues values = new ContentValues();
        values.put(DBManager.Pkg.COL_PACKAGE, pkgName);
        return db.insertData(DBManager.Pkg.TABLE, values);
    }

    public static long insertInToAppLockerTable(String pkgName, DBManager db) {
        ContentValues values = new ContentValues();
        values.put(DBManager.AppLocker.COL_PACKAGE, pkgName);
        return db.insertData(DBManager.AppLocker.TABLE, values);
    }

    public static void deleteFromAppLockerTable(String pkgName, DBManager db) {
        String where = DBManager.AppLocker.COL_PACKAGE + " = ?";
        String[] whereArgs = new String[] {
                pkgName
        };
        db.delete(DBManager.AppLocker.TABLE, where, whereArgs);
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static Notification createNotification(Context context) {
        Intent nextIntent = new Intent(context, AppLockerService.class);
        // nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(context, 0,
                nextIntent, 0);
        NotificationCompat.Builder notificationI = new NotificationCompat.Builder(context)
                .setContentTitle("")
                .setTicker("")
                .setContentText("")
                .setSmallIcon(R.mipmap.applock_blank_icon)
                .setAutoCancel(false)
                .setContentIntent(pnextIntent);

        /*
         * Notification notification = notificationI.build(); notification.priority =
         * Notification.PRIORITY_MIN;
         */
        // notification.visibility = Notification.VISIBILITY_SECRET;
        // notification.setLatestEventInfo( this, title, text, contentIntent );
        Notification notification = notificationI.build();
        notification.priority = Notification.PRIORITY_MIN;
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int smallIconViewId = context.getResources().getIdentifier("right_icon", "id",
                    android.R.class.getPackage().getName());

            if (smallIconViewId != 0) {
                if (notification.contentIntent != null)
                    notification.contentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                if (notification.headsUpContentView != null)
                    notification.headsUpContentView.setViewVisibility(smallIconViewId,
                            View.INVISIBLE);

                if (notification.bigContentView != null)
                    notification.bigContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);
            }
        }*/
        return notification;
    }



    public static ArrayList<String> getLockedApp(DBManager db) {
        ArrayList<String> list = new ArrayList<>();
        Cursor cursor = db.selectCursor("select * from " + DBManager.AppLocker.TABLE);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String packageName = cursor
                        .getString(cursor.getColumnIndex(DBManager.AppLocker.COL_PACKAGE));
                list.add(packageName);
            } while (cursor.moveToNext());
        }
        return list;

    }


    public static String getAppName(Context ctx, String pkgId, DBManager db) {
        String appName;
        String pkgName;
        pkgName = getPkgName(pkgId, db);

        if (pkgName == null) {
            return null;
        }

        PackageManager packageManager = ctx.getPackageManager();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(pkgName, APP_INFO_FLAG);
        } catch (final PackageManager.NameNotFoundException e) {
            return pkgName;
        }
        // appName = (String) ((applicationInfo != null) ?
        // packageManager.getApplicationLabel(applicationInfo) : APP_DEFAULT_NAME_STRING);
        if (applicationInfo != null) {
            appName = (String) packageManager.getApplicationLabel(applicationInfo);
        } else {
            appName = (String) APP_DEFAULT_NAME_STRING;
        }
        return appName;
    }

    public static String getPkgName(String pkgId, DBManager db) {
        String pkgName = null;

        if (db != null) {
            String query = "select " + DBManager.Pkg.COL_PACKAGE + " FROM " + DBManager.Pkg.TABLE
                    + " WHERE " + DBManager.Pkg.COL_ID + " ='" + pkgId + "'";
            Cursor cursor = db.doRawQuery(query);

            if (cursor != null && cursor.moveToFirst()) {
                pkgName = cursor.getString(0);
            }

            if (cursor != null) {
                cursor.close();
            }
        }

        return pkgName;
    }

    private static boolean isExistInfectedFile(String filePath) {
        boolean isExist = false;

        File file = new File(filePath);
        if (file.exists()) {
            isExist = true;
        }
        return isExist;
    }

    private static boolean isInstalledInfectedApp(Context ctx, String packageName) {
        PackageManager pm = ctx.getPackageManager();
        boolean appInstalled = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            appInstalled = false;
        }
        return appInstalled;

    }

    public static String getAppNameFromPkg(Context context, String packageName) {
        final PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        if (ai != null) {
            return (String) pm.getApplicationLabel(ai);
        } else {
            return "(unknown)";
        }
        // return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }

    public static Drawable getAppIconFromPath(Context context, String filePath) {
        Drawable icon = null;
        PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(filePath,
                PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            if (Build.VERSION.SDK_INT >= 8) {
                appInfo.sourceDir = filePath;
                appInfo.publicSourceDir = filePath;
            }
            icon = appInfo.loadIcon(context.getPackageManager());
        }
        return icon;
    }

    public static boolean isLicenseExpired(Context context) {
        boolean isLicenseExpired = Preferences.loadBoolean(context, Preferences.KEY_LICENSE_EXPIRED,
                false);
        if (isLicenseExpired) {
            return true;
        }
        long expTime = Preferences.loadLong(context, Preferences.KEY_EXPIRATION_TIME_LONG, 0);
        if (expTime == 0) {
            return false;
        }
        return System.currentTimeMillis() >= expTime;
    }

    public static boolean isMaxLicenseExceeded(Context context) {
        return Preferences.loadBoolean(context, Preferences.EXCEED_RANGE_LICENSE_NO, false);
    }



    public static boolean uninstalledAtleastOne(ArrayList<String> mInfectedApps, Context context) {
        PackageManager packageManager = context.getPackageManager();
        int nPkgs = mInfectedApps.size();
        for (int i = 0; i < nPkgs; i++) {
            try {
                packageManager.getPackageInfo(mInfectedApps.get(i), PackageManager.GET_ACTIVITIES);
            } catch (PackageManager.NameNotFoundException e) {
                return true;
            }
        }
        return false;
    }



    private static long compareExpireCheckTimewithCurTime(Context context,
            long nextExpirationCheckTime) {
        long currentTime = System.currentTimeMillis();
        if (nextExpirationCheckTime > currentTime + ADD_24_HOURS_IN_MILISEC) {
            nextExpirationCheckTime = currentTime + ADD_24_HOURS_IN_MILISEC;
        }
        while (nextExpirationCheckTime < currentTime) {
            nextExpirationCheckTime += ADD_24_HOURS_IN_MILISEC;
        }
        Preferences.save(context, Preferences.KEY_NEXT_EXPIRATION_CHK_TIME,
                nextExpirationCheckTime);
        return nextExpirationCheckTime;
    }



    public static String getEthernetMacAddress() {
        String filePath = ("/sys/class/net/eth0/address");
        try {
            StringBuffer fileData = new StringBuffer(1000);
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();
            String lanMacAddress = fileData.toString().toUpperCase().substring(0, 17);
            Log.d("lanMacAddress", lanMacAddress);
            return lanMacAddress;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
