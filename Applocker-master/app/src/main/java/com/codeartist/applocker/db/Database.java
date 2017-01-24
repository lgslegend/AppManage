package com.codeartist.applocker.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class Database {

    private int mFirstVersion = 1;
    private String mDbName = "devact_applockDb";
    private DBAccessLatchCounter mDBAccessLatchCounter = null;
    private SQLiteOpenHelper mHelper = null;
    private SQLiteDatabase mDb = null;

    public Database(Context context) {
        this.mHelper = new DBHelper(context, mDbName, mFirstVersion);
        mDBAccessLatchCounter = DBAccessLatchCounter.getInstance();
    }

    public synchronized SQLiteDatabase open() {
        if (mDb == null) {
            mDb = mHelper.getReadableDatabase();
        }
        mDBAccessLatchCounter.openCount();
        return mDb;
    }

    public synchronized void close() {
        mDBAccessLatchCounter.closeCount();

        if (mDBAccessLatchCounter.canClose()) {
            if (mDb != null) {
                mDb.close();
                mDb = null;
            }
            mHelper.close();
        }
    }

    protected Cursor doRawQuery(SQLiteDatabase db, String query) {
        return db.rawQuery(query, null);
    }

    protected Cursor doQuery(SQLiteDatabase db, String tableName, String[] columns,
                             String selection, String[] selectionArgs, String groupBy, String having,
                             String orderBy, String limit) {
        return db.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy,
                limit);
    }

    protected Cursor doQuery(SQLiteDatabase db, String tableName, String[] columns,
                             String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return db.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    protected Cursor doQuery(SQLiteDatabase db, String tableName, String[] columns,
                             String selection, String[] selectionArgs, String groupBy, String having) {
        return db.query(tableName, columns, selection, selectionArgs, groupBy, having, null);
    }

    protected Cursor doQuery(SQLiteDatabase db, String tableName, String[] columns,
                             String selection, String[] selectionArgs, String groupBy) {
        return db.query(tableName, columns, selection, selectionArgs, groupBy, null, null);
    }

    protected Cursor doQuery(SQLiteDatabase db, String tableName, String[] columns,
                             String selection, String[] selectionArgs) {
        return db.query(tableName, columns, selection, selectionArgs, null, null, null);
    }

    protected Cursor doQuery(SQLiteDatabase db, String tableName, String[] columns) {
        return db.query(tableName, columns, null, null, null, null, null);

    }

    protected Cursor doQuery(SQLiteDatabase db, String tableName) {
        return db.query(tableName, null, null, null, null, null, null);

    }

    public long insertData(SQLiteDatabase db, String tableName, ContentValues values) {
        return db.insert(tableName, null, values);
    }

    public void insertDataWithTransction(SQLiteDatabase db, String tableName, ContentValues values) {
        db.beginTransaction();
        db.insert(tableName, null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void insertDataWithTransaction(SQLiteDatabase db, String tableName,
                                          ArrayList<ContentValues> values) {
        db.beginTransaction();
        for (ContentValues value : values) {
            db.insert(tableName, null, value);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void beginTransaction(SQLiteDatabase db) {
        db.beginTransaction();
    }

    public void endTransaction(SQLiteDatabase db) {
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void delete(SQLiteDatabase db, String tableName, String whereClause, String[] whereArgs) {
        db.delete(tableName, whereClause, whereArgs);
    }

    public void update(SQLiteDatabase db, String tableName, ContentValues values,
                       String whereClause, String[] whereArgs) {
        db.update(tableName, values, whereClause, whereArgs);
    }

    static class DBAccessLatchCounter {
        static DBAccessLatchCounter instance = null;
        private int count = 0;

        synchronized static DBAccessLatchCounter getInstance() {
            if (instance == null) {
                instance = new DBAccessLatchCounter();
            }
            return instance;
        }

        void openCount() {
            count++;
        }

        void closeCount() {
            count--;
        }

        boolean canClose() {
            return count == 0;
        }
    }
}
