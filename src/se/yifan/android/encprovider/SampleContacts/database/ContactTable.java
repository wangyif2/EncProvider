package se.yifan.android.encprovider.SampleContacts.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ContactTable {

    // Database table
    public static final String TABLE_CONTACTS = "contacts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_AGE = "age";

    // Database creation SQL statement
    public static final String DATABASE_CREATE = "create table if not exists "
            + TABLE_CONTACTS
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_EMAIL + " text not null, "
            + COLUMN_AGE + " integer not null" + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(ContactTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(database);
    }
}
