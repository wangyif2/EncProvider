package se.yifan.android.encprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;
import se.yifan.android.encprovider.SampleContacts.database.ContactTable;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * User: robert
 * Date: 12/01/13
 */
public class EncProvider extends ContentProvider {
    public static String dbName;
    public static final String serverHostname = "142.1.207.248";
    public static final int serverPort = 1111;

    public static QueryPacket fromServer;
    public static QueryPacket toServer;
    private Gson gson = new Gson();

    Socket serverSocket = null;
    public static ObjectOutputStream out = null;
    public static ObjectInputStream in = null;

    @Override
    public boolean onCreate() {
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
        long startTime = System.currentTimeMillis();
        dbName = databaseName;

        new setupNetwork().execute();

        toServer = new QueryPacket();
        toServer.type = QueryPacket.DB_CREATE;
        toServer.db_creation = databaseCreationScript;
        toServer.db_table = ContactTable.TABLE_CONTACTS;
        toServer.db_name = dbName;

        new EncNetworkHandler() {
            @Override
            public void onPostExecute(QueryPacket result) {
                fromServer = result;
            }
        }.execute(toServer);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        Log.i("EncProvider-timeLog", "Time spend in onCreate: " + duration);
    }

    public HashMap<Integer, byte[]> query(String sql, String[] selectionArgs) {
        long startTime = System.currentTimeMillis();
        Log.i("EncProvider", "Query: \n\twith sql: " + sql + "\n\tselectionArgs: " + Arrays.toString(selectionArgs));
        toServer = new QueryPacket();
        toServer.type = QueryPacket.DB_QUERY;
        toServer.db_name = dbName;
        toServer.db_query = sql;
        toServer.args = selectionArgs;

        EncNetworkHandler en = (EncNetworkHandler) new EncNetworkHandler().execute(toServer);
        try {
            fromServer = en.get(5000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Log.i("EncProvider", "Query encKey is: " + fromServer.encKey);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        Log.i("EncProvider-timeLog", "Time spend in query: " + duration);

        return fromServer.encKey;
    }

    public void insert(String tableContacts, String nullColumnHack, ContentValues contentValues) {
        long startTime = System.currentTimeMillis();
        Log.i("EncProvider", "Insert: \n\twith table: " + tableContacts + "\n\tContentValues: " + contentValues.toString());

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
            fromServer = en.get(5000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Log.i("EncProvider", "EncProvider: Insert: encContentValues is: " + fromServer.encContentValues);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        Log.i("EncProvider-timeLog", "Time spend in insert: " + duration);

        HashMap<String, byte[]> encContentValues = fromServer.encContentValues;
        int replySize = (encContentValues != null && encContentValues.size() > 0) ? encContentValues.size() : 0;
        if (replySize > 0) {
            for (String colName : encContentValues.keySet()) {
                contentValues.put(colName, encContentValues.get(colName));
            }
        }
    }

    public void delete(String tableContacts, String whereClause, String[] whereArgs) {
        long startTime = System.currentTimeMillis();
        if (whereClause == null || whereArgs == null)
            Log.i("EncProvider", "Delete: \n\twith table: " + tableContacts + "\n\twhereClause: null\n\twhereArgs: null");
        else
            Log.i("EncProvider", "Delete: \n\twith table: " + tableContacts + "\n\twhereClause: " + whereClause + "\n\twhereArgs:" + whereArgs.toString());

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

        Log.i("EncProvider", "Delete: encContentValues is: " + fromServer.encContentValues);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        Log.i("EncProvider-timeLog", "Time spend in delete: " + duration);
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

    public static MatrixCursor decryptLocalQuery(Cursor cursor, HashMap<Integer, byte[]> decryptionSet) {
        ArrayList<String> columns = new ArrayList<String>();
        int indexOfId = cursor.getColumnIndex("_id");
        int columnCount = cursor.getColumnCount();

        for (int i = 0; i < columnCount; i++) {
            columns.add(cursor.getColumnName(i));
        }

        String[] columnName = columns.toArray(new String[0]);
        MatrixCursor m = new MatrixCursor(columnName);

        while (!cursor.isAfterLast()) {
            int localId = cursor.getInt(indexOfId);
            byte[] key = decryptionSet.get(localId);
            SecretKey k = new SecretKeySpec(key,"AES");
            ArrayList<Object> row = new ArrayList<Object>();
            row.add(cursor.getInt(0));
            for (int i = 1; i < columnCount; i++) {
                try {
                    String s = EncUtil.decryptMsg(cursor.getBlob(i), k);
                    row.add(s);
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidParameterSpecException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            m.addRow(row.toArray(new Object[0]));
            cursor.moveToNext();
        }
        return m;
    }

    class setupNetwork extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            InetAddress hostIp = null;
            try {
                hostIp = InetAddress.getByName(serverHostname);
                serverSocket = new Socket(hostIp, EncProvider.serverPort);
                out = new ObjectOutputStream(serverSocket.getOutputStream());
                in = new ObjectInputStream(serverSocket.getInputStream());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
