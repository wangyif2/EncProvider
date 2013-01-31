package se.yifan.android.encprovider.SampleContacts.contentprovider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import se.yifan.android.encprovider.EncProvider;
import se.yifan.android.encprovider.SampleContacts.database.ContactDatabaseHelper;
import se.yifan.android.encprovider.SampleContacts.database.ContactTable;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class ContactProvider extends EncProvider {

    private static final boolean DEBUG = true;

    // database
    private ContactDatabaseHelper database;

    // Used for the UriMacher
    private static final int CONTACTS = 10;
    private static final int CONTACT_ID = 20;

    // Content URI
    private static final String AUTHORITY = "se.yifan.android.encprovider.SampleContacts.contentprovider";
    private static final String BASE_PATH = "contacts";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    // MIME type for multiple rows
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/contacts";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/contact";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, CONTACTS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CONTACT_ID);
    }

    @Override
    public boolean onCreate() {
        try {
            super.onCreate(ContactTable.DATABASE_CREATE, ContactDatabaseHelper.DATABASE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (DEBUG) Log.d("EncProvider", "ContactProvider: in onCreate.");

        database = new ContactDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (DEBUG) Log.d("EncProvider", "ContactProvider: in QUERY with URI: " + uri.toString());

        checkColumns(projection);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(ContactTable.TABLE_CONTACTS);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case CONTACTS:
                break;
            case CONTACT_ID:
                queryBuilder.appendWhere(ContactTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        //get readable?
        SQLiteDatabase db = database.getWritableDatabase();
        super.query(queryBuilder.buildQuery(projection, selection, null, null, sortOrder, null),
                selectionArgs);
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (DEBUG) Log.i("EncProvider", "ContactProvider: in INSERT with URI: " + uri.toString());

        int uriType = sURIMatcher.match(uri);
        if (DEBUG) Log.i("EncProvider", "ContactProvider: in INSERT with URItype: " + uriType);

        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case CONTACTS:
                super.insert(ContactTable.TABLE_CONTACTS, null, values);
                id = sqlDB.insert(ContactTable.TABLE_CONTACTS, null, values);
                if (DEBUG) Log.i("EncProvider", "ContactProvider: in INSERT with returned id: " + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        super.delete(uri, selection, selectionArgs);

        if (DEBUG) Log.i("EncProvider", "ContactProvider: in DELETE with URI: " + uri.toString());

        int uriType = sURIMatcher.match(uri);
        if (DEBUG) Log.i("EncProvider", "ContactProvider: in DELETE with URItype: " + uriType);

        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case CONTACTS:
                super.delete(ContactTable.TABLE_CONTACTS, selection, selectionArgs);
                rowsDeleted = sqlDB.delete(ContactTable.TABLE_CONTACTS, selection, selectionArgs);
                break;
            case CONTACT_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    super.delete(ContactTable.TABLE_CONTACTS, ContactTable.COLUMN_ID + "=" + id, null);
                    rowsDeleted = sqlDB.delete(ContactTable.TABLE_CONTACTS, ContactTable.COLUMN_ID + "=" + id, null);
                } else {
                    super.delete(ContactTable.TABLE_CONTACTS, ContactTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                    rowsDeleted = sqlDB.delete(ContactTable.TABLE_CONTACTS, ContactTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        super.update(uri, values, selection, selectionArgs);

        if (DEBUG) Log.i("EncProvider", "ContactProvider: in UPDATE with URI: " + uri.toString());

        int uriType = sURIMatcher.match(uri);

        if (DEBUG) Log.i("EncProvider", "ContactProvider: in UPDATE with URItype: " + uriType);

        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case CONTACTS:
                rowsUpdated = sqlDB.update(ContactTable.TABLE_CONTACTS, values, selection, selectionArgs);
                break;
            case CONTACT_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(ContactTable.TABLE_CONTACTS, values, ContactTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = sqlDB.update(ContactTable.TABLE_CONTACTS, values, ContactTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {ContactTable.COLUMN_NAME,
                ContactTable.COLUMN_EMAIL,
                ContactTable.COLUMN_AGE,
                ContactTable.COLUMN_ID};
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException(
                        "Unknown columns in projection");
            }
        }
    }

}
