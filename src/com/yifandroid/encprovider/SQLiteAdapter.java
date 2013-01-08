package com.yifandroid.encprovider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * User: robert
 * Date: 06/01/13
 */
public class SQLiteAdapter {
    public static final String MY_ENC_DATABASE = "ENC_DATABASE";
    public static final String MY_ENC_DATABASE_TABLE = "MY_ENC_TABLE";
    public static final int MY_ENC_DATABASE_VERSION = 1;
    public static final String KEY_ID = "_id";
    public static final String KEY_CONTENT = "Content";

    private static final String SCRIPT_CREATE_DATABASE =
            "create table " + MY_ENC_DATABASE_TABLE + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_CONTENT + " TEXT NOT NULL);";

    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;

    private Context context;

    public SQLiteAdapter(Context context) {
        this.context = context;
    }

    public SQLiteAdapter openToRead() throws SQLException {
        sqLiteHelper = new SQLiteHelper(context, MY_ENC_DATABASE, null, MY_ENC_DATABASE_VERSION);
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        return this;
    }

    public SQLiteAdapter openToWrite() throws SQLException {
        sqLiteHelper = new SQLiteHelper(context, MY_ENC_DATABASE, null, MY_ENC_DATABASE_VERSION);
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        sqLiteHelper.close();
    }

    public long insert(String content) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_CONTENT, content);
        return sqLiteDatabase.insert(MY_ENC_DATABASE_TABLE, null, contentValues);
    }

    public Cursor queueAll() {
        String[] columns = new String[]{KEY_ID, KEY_CONTENT};
        return sqLiteDatabase.query(MY_ENC_DATABASE_TABLE, columns, null, null, null, null, null);
    }

    public int deleteAll() {
        return sqLiteDatabase.delete(MY_ENC_DATABASE_TABLE, null, null);
    }

    public class SQLiteHelper extends SQLiteOpenHelper {

        public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(SCRIPT_CREATE_DATABASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }
}
