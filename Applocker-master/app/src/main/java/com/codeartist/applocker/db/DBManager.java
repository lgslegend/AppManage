
package com.codeartist.applocker.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

final public class DBManager extends Database {
    private static DBManager sInstance = null;

    private SQLiteDatabase mDb = null;

    private DBManager(Context context) {
        super(context);
        mDb = open();
    }

    public static DBManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DBManager.class) {
                if (sInstance == null) {
                    sInstance = new DBManager(context);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void close() {
        super.close();
    }

    public Cursor doRawQuery(String query) {

        return doRawQuery(mDb, query);
    }

    public Cursor doQuery(String tableName, String[] columns, String selection,
                          String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {

        return doQuery(mDb, tableName, columns, selection, selectionArgs, groupBy, having, orderBy,
                limit);
    }

    public Cursor doQuery(String tableName, String[] columns, String selection,
                          String[] selectionArgs, String groupBy, String having, String orderBy) {
        return doQuery(mDb, tableName, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public Cursor doQuery(String tableName, String[] columns, String selection,
                          String[] selectionArgs, String groupBy, String having) {
        return doQuery(mDb, tableName, columns, selection, selectionArgs, groupBy, having);
    }

    public Cursor doQuery(String tableName, String[] columns, String selection,
                          String[] selectionArgs, String groupBy) {
        return doQuery(mDb, tableName, columns, selection, selectionArgs, groupBy, null, null);
    }

    public Cursor doQuery(String tableName, String[] columns, String selection,
                          String[] selectionArgs) {
        return doQuery(mDb, tableName, columns, selection, selectionArgs, null, null, null);
    }

    public Cursor doQuery(String tableName, String[] columns) {
        return doQuery(mDb, tableName, columns);
    }

    public Cursor doQuery(String tableName) {
        return doQuery(mDb, tableName);
    }

    public long insertData(String tableName, ContentValues values) {
        return insertData(mDb, tableName, values);
    }

    public void insertDataWithTransaction(String tableName, ContentValues values) {
        insertDataWithTransction(mDb, tableName, values);
    }

    public void insertDataWithTransaction(String tableName, ArrayList<ContentValues> values) {
        insertDataWithTransaction(mDb, tableName, values);
    }

    public void delete(String tableName, String whereClause, String[] whereArgs) {
        delete(mDb, tableName, whereClause, whereArgs);
    }

    public void update(String tableName, ContentValues values, String whereClause,
                       String[] whereArgs) {
        update(mDb, tableName, values, whereClause, whereArgs);
    }

    public Cursor selectCursor(String sql) {
        return mDb.rawQuery(sql, null);
    }

    public class Pkg {

        public static final String COL_ID = "_id";
        public static final String COL_PACKAGE = "pkg_name";
        public static final String TABLE = "pkg";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE + "(" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                COL_PACKAGE + " TEXT" +
                ")";
    }

    public class AppLocker {
        public static final String COL_PACKAGE = "pkg_name";
        public static final String TABLE = "app_locker";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + TABLE + "(" +
                COL_PACKAGE + " TEXT PRIMARY KEY " +
                ")";

    }

    public class ScanResult {

        public static final String COL_ID = "_id";
        public static final String COL_PACKAGE_ID = "pkg_id";
        public static final String COL_SCAN_TIME = "scan_time";
        public static final String COL_SCAN_TYPE = "scan_type";
        public static final String COL_SCAN_STATUS = "scan_status";
        public static final String TABLE = "scanresult";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE + "(" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                COL_PACKAGE_ID + " TEXT" + "," +
                COL_SCAN_TIME + " TEXT" + "," +
                COL_SCAN_TYPE + " INTEGER" + "," +
                COL_SCAN_STATUS + " INTEGER" +
                ")";

    }
}
