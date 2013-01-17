package se.yifan.android.encprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * User: robert
 * Date: 12/01/13
 */
public class EncProvider extends ContentProvider {
    public static final String serverHostname = "142.1.162.151";
    public static final int serverPort = 8080;
    private Socket serverSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    public static QueryPacket fromServer;
    public static QueryPacket toServer;

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
        Log.i("EncProvider", this.getClass().getName() + this.getContext().toString());
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

        new NetworkHandler() {
            @Override
            public void onPostExecute(QueryPacket result) {
                fromServer = result;
            }
        }.execute(toServer);
    }

    public class NetworkHandler extends AsyncTask<QueryPacket, Integer, QueryPacket> {

        @Override
        protected QueryPacket doInBackground(QueryPacket... packetToServer) {
            QueryPacket packetFromServer = null;
            try {
                InetAddress hostIp = InetAddress.getByName(serverHostname);

                serverSocket = new Socket(hostIp, EncProvider.serverPort);

                Log.i("EncProvider", "Connecting to Server with Packet type: " + toServer.type);

                out = new ObjectOutputStream(serverSocket.getOutputStream());
                in = new ObjectInputStream(serverSocket.getInputStream());

                out.writeObject(packetToServer[0]);

                packetFromServer = (QueryPacket) in.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return packetFromServer;
        }

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
