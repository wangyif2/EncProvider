package se.yifan.android.encprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;

import java.io.IOException;

/**
 * User: robert
 * Date: 12/01/13
 */
public class EncProvider extends ContentProvider {
    public static final String serverHostname = "142.1.130.40";
    public static final int serverPort = 1111;

    public static QueryPacket fromServer;
    public static QueryPacket toServer;
    private Gson gson;

    @Override
    public boolean onCreate() {
        Log.i("EncProvider", "in onCreate");
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
        Log.i("EncProvider", "In method Insert: with URI\n" + uri.toString() + "\nContentValues:\n" + contentValues.toString());

        gson = new Gson();

        toServer = new QueryPacket();
        toServer.type = QueryPacket.DB_INSERT;
        toServer.uri = uri.toString();
        toServer.contentValues = gson.toJson(contentValues);

        new EncNetworkHandler() {
            @Override
            public void onPostExecute(QueryPacket result) {
                fromServer = result;
            }
        }.execute(toServer);

//        contentValues.put("key", fromServer.key);

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
        Log.i("EncProvider", "EncProvider in onCreate with databaseCreation Script: \n" + databaseCreationScript
                + "\n databaseName: " + databaseName);

        toServer = new QueryPacket();
        toServer.type = QueryPacket.DB_CREATE;
        toServer.db_creation = databaseCreationScript;
        toServer.db_name = databaseName;

        new EncNetworkHandler() {
            @Override
            public void onPostExecute(QueryPacket result) {
                fromServer = result;
            }
        }.execute(toServer);
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
}
