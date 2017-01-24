package com.codeartist.applocker.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

    public static final String LAST_SCAN_TIME = "LAST_SCAN_TIME";
    public static final String LAST_SCAN_RESULT = "LAST_SCAN_RESULT";
    public static final String LICENSE_REGISTRATION_COMPLETED = "license_registration";
    public static final String EMAIL_ID = "emailId";
    public static final String LAST_SCAN_VIRUS_COUNT = "total_virus";

    public static final String KEY_IS_MOUNT_NEED_TO_BE_SET = "key_need_to_be_set";
    public static final String KEY_EXPIRATION_TIME_LONG = "expiration_time_long";
    public static final String KEY_NEXT_EXPIRATION_CHK_TIME = "next_expiration_time_check";
    public static final String LICENSE_KEY = "license_key";
    public static final String EXCEED_RANGE_LICENSE_NO = "max_range_of_license_exceeded";
    public static final String KEY_LICENSE_EXPIRED = "license_expired";
    public static final String KEY_APP_LOCKER_PASSWORD = "locker_password";

    static private SharedPreferences Manager(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * boolean値の保存
     *
     * @param context
     * @param key
     * @param value
     */
    static public void save(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = Manager(context).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * float値の保存
     *
     * @param context
     * @param key
     * @param value
     */
    static public void save(Context context, String key, float value) {
        SharedPreferences.Editor editor = Manager(context).edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    /**
     * @param context
     * @param key
     * @param value
     */
    static public void save(Context context, String key, int value) {
        SharedPreferences.Editor editor = Manager(context).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * @param context
     * @param key
     * @param value
     */
    static public void save(Context context, String key, long value) {
        SharedPreferences.Editor editor = Manager(context).edit();
        editor.putLong(key, value);
        editor.commit();
    }

    /**
     * String
     *
     * @param context
     * @param key
     * @param value
     */
    static public void save(Context context, String key, String value) {
        SharedPreferences.Editor editor = Manager(context).edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * boolean値の読み出し
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    static public boolean loadBoolean(Context context, String key, boolean defaultValue) {
        return Manager(context).getBoolean(key, defaultValue);
    }

    /**
     * float値の読み出し
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    static public float loadFloat(Context context, String key, float defaultValue) {
        return Manager(context).getFloat(key, defaultValue);
    }

    /**
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    static public int loadInt(Context context, String key, int defaultValue) {
        return Manager(context).getInt(key, defaultValue);
    }

    /**
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    static public long loadLong(Context context, String key, long defaultValue) {
        return Manager(context).getLong(key, defaultValue);
    }

    /**
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    static public String loadString(Context context, String key, String defaultValue) {
        return Manager(context).getString(key, defaultValue);
    }

    static public boolean contains(Context context, String key) {
        return Manager(context).contains(key);
    }

    static public void remove(Context context, String key) {
        SharedPreferences.Editor editor = Manager(context).edit();
        editor.remove(key);
        editor.commit();
    }


}
