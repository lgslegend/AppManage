package com.codeartist.applocker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static String strDatabaseName = null;
    private Object[] mTableStrings = {DBManager.Pkg.SQL_CREATE_TABLE, DBManager.ScanResult.SQL_CREATE_TABLE, DBManager.AppLocker.SQL_CREATE_TABLE};
    private Context mContext;

    public DBHelper(Context context, String dbName, int version) {
        super(context, dbName, null, version);
        strDatabaseName = dbName;
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // db.execSQL(sql);
        if (mTableStrings == null) {
            throw new RuntimeException("STB table string array is not provided");
        }
        for (Object s : mTableStrings) {
            db.execSQL(s.toString());

        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // if db version is changed please handle here. If Phase2 comes
        // and
        // need to change the db then must need to handle here.

    }

    public String getDbName() {
        return strDatabaseName;
    }

}
