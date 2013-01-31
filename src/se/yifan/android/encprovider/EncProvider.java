package se.yifan.android.encprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * User: robert
 * Date: 12/01/13
 */
public class EncProvider extends ContentProvider {
    public static String dbName;
    public static final String serverHostname = "142.1.132.158";
    public static final int serverPort = 1111;

    public static QueryPacket fromServer;
    public static QueryPacket toServer;
    private Gson gson;

    @Override
    public boolean onCreate() {
        Log.i("EncProvider", "EncProvider: onCreate");
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    public void onCreate(String databaseCreationScript, String databaseName) throws IOException, ClassNotFoundException {
        Log.i("EncProvider", "EncProvider: onCreate with databaseCreation Script: \n" + databaseCreationScript
                + "\n databaseName: " + databaseName);
        dbName = databaseName;

        toServer = new QueryPacket();
        toServer.type = QueryPacket.DB_CREATE;
        toServer.db_creation = databaseCreationScript;
        toServer.db_name = dbName;

        new EncNetworkHandler() {
            @Override
            public void onPostExecute(QueryPacket result) {
                fromServer = result;
            }
        }.execute(toServer);
    }

    public String[] query(String sql, String[] selectionArgs) {
        toServer = new QueryPacket();
        toServer.type = QueryPacket.DB_QUERY;
        toServer.db_name = dbName;
        toServer.db_query = sql;
        toServer.args = selectionArgs;

        EncNetworkHandler en = (EncNetworkHandler) new EncNetworkHandler().execute(toServer);
        try {
//            Toast toastBegin = Toast.makeText(this.getContext(), "Contacting Server...", 5000);
//            toastBegin.show();
            fromServer = en.get(5000, TimeUnit.SECONDS);
//            Toast toastEnd = Toast.makeText(this.getContext(), "Response received...", 1);
//            toastEnd.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Log.i("EncProvider", "EncProvider: Insert: key is: " + fromServer.key);

        return new String[0];
    }

    public void insert(String tableContacts, String nullColumnHack, ContentValues contentValues) {
        Log.i("EncProvider", "EncProvider: Insert: with table\n" + tableContacts + "\nContentValues:\n" + contentValues.toString());

        gson = new Gson();

        toServer = new QueryPacket();
        toServer.type = QueryPacket.DB_INSERT;
        toServer.db_name = dbName;
        toServer.table = tableContacts;
        toServer.contentValues = gson.toJson(contentValues);

        int[] bindArgsType = null;
        int size = (contentValues != null && contentValues.size() > 0) ? contentValues.size() : 0;
        if (size > 0) {
            bindArgsType = new int[size];
            int i = 0;
            for (String colName : contentValues.keySet()) {
                bindArgsType[i++] = getTypeOfObject(contentValues.get(colName));
            }
            toServer.contentType = bindArgsType;
        }
        toServer.nullColumnHack = nullColumnHack;

        EncNetworkHandler en = (EncNetworkHandler) new EncNetworkHandler().execute(toServer);
        try {
            Toast toastBegin = Toast.makeText(this.getContext(), "Contacting Server...", 5000);
            toastBegin.show();
            fromServer = en.get(5000, TimeUnit.SECONDS);
            Toast toastEnd = Toast.makeText(this.getContext(), "Response received...", 1);
            toastEnd.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Log.i("EncProvider", "EncProvider: Insert: key is: " + fromServer.key);

//        contentValues.put("key", fromServer.key);
    }

    public void delete(String tableContacts, String whereClause, String[] whereArgs) {
        if (whereClause == null || whereArgs == null)
            Log.i("EncProvider", "EncProvider: Delete: with table\n" + tableContacts + "\nwhereClause: null\nwhereArgs: null");
        else
            Log.i("EncProvider", "EncProvider: Delete: with table\n" + tableContacts + "\nwhereClause:\n" + whereClause + "\nwhereArgs:\n" + whereArgs.toString());

        toServer = new QueryPacket();
        toServer.type = QueryPacket.DB_DELETE;
        toServer.db_name = dbName;
        toServer.table = tableContacts;
        toServer.whereClause = whereClause;
        toServer.args = whereArgs;

        EncNetworkHandler en = (EncNetworkHandler) new EncNetworkHandler().execute(toServer);
        try {
            Toast toastBegin = Toast.makeText(this.getContext(), "Contacting Server...", 5000);
            toastBegin.show();
            fromServer = en.get(5000, TimeUnit.SECONDS);
            Toast toastEnd = Toast.makeText(this.getContext(), "Response received...", 1);
            toastEnd.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Log.i("EncProvider", "EncProvider: Delete: key is: " + fromServer.key);
    }

    public static QueryPacket getToServer() {
        return toServer;
    }

    public static QueryPacket getFromServer() {
        return fromServer;
    }

    public static void setFromServer(QueryPacket fromServer) {
        EncProvider.fromServer = fromServer;
    }

    public static void setToServer(QueryPacket toServer) {
        EncProvider.toServer = toServer;
    }

    public static int getTypeOfObject(Object obj) {
        if (obj == null) {
            return Cursor.FIELD_TYPE_NULL;
        } else if (obj instanceof byte[]) {
            return Cursor.FIELD_TYPE_BLOB;
        } else if (obj instanceof Float || obj instanceof Double) {
            return Cursor.FIELD_TYPE_FLOAT;
        } else if (obj instanceof Long || obj instanceof Integer
                || obj instanceof Short || obj instanceof Byte) {
            return Cursor.FIELD_TYPE_INTEGER;
        } else {
            return Cursor.FIELD_TYPE_STRING;
        }
    }
}
