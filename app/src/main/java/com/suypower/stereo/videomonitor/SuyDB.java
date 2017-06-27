package com.suypower.stereo.videomonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Administrator on 14-6-5.
 */
public class SuyDB {

    public static String DBPath;

    private String dbpath = "";
    private SQLiteDatabase readdb;
    private SQLiteDatabase db;
    private SQLiteOpenHelper sqLiteOpenHelper;
    static SuyDB suyDB;

    public static SuyDB getSuyDB() {
        return suyDB;
    }

    public static void setSuyDB(SuyDB suyDB) {
        SuyDB.suyDB = suyDB;
    }

    public String getDbpath() {
        return dbpath;
    }

    public void setDbpath(String dbpath) {
        this.dbpath = dbpath;
    }

    public SuyDB(Context context, String dbpath, Boolean isread) {
        sqLiteOpenHelper = new SQLiteOpenHelper(context, dbpath, null, 3) {
            @Override
            public void onCreate(SQLiteDatabase sqLiteDatabase) {

            }

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

            }
        };

        this.dbpath = dbpath;
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        if (isread)
            readdb = sqLiteOpenHelper.getReadableDatabase();
        else
            db = sqLiteOpenHelper.getWritableDatabase();
    }


    public void closeDB() {
        if (db != null)
            db.close();
    }


    public SQLiteDatabase getReaddb() {
        return readdb;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public SQLiteOpenHelper getSqLiteOpenHelper() {
        return sqLiteOpenHelper;
    }




}
