package tech.xiaosuo.com.phontomanager.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import tech.xiaosuo.com.phontomanager.bean.PhotoInfoTable;

/**
 * Created by wangshumin on 6/6/2018.
 */

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DataBaseHelper";
/*    public static final String CREATE_BOOK = "create table Book (" +
            "id integer primary key autoincrement, " +
            "author text, " +
            "price real, " +
            "page integer, " +
            "name text)";*/
    private static final String DATABASE_NAME = "photo.db";
    public static final int VERSION = 1;

    private final String CREATE_PHOTO_INFO_TABLE ="create table " + PhotoInfoTable.PHOTO_INFO_TABLE + "( "
            + PhotoInfoTable._ID + " integer primary key autoincrement, "
            + PhotoInfoTable.COLUMN_IMAGE_NAME + " text, "
            + PhotoInfoTable.COLUMN_IMAGE_DATA + " text, "
            + PhotoInfoTable.COLUMN_IMAGE_MD5 + " text, "
            + PhotoInfoTable.COLUMN_IMAGE_IS_IN_CLOUD + " integer "
            + ")";

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, version);
    }

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG," database helper create db");
        db.execSQL(CREATE_PHOTO_INFO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
