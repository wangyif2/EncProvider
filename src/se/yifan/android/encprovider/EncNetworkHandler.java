package se.yifan.android.encprovider;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * User: robert
 * Date: 26/01/13
 */
public class EncNetworkHandler extends AsyncTask<QueryPacket, Integer, QueryPacket> {

    @Override
    protected QueryPacket doInBackground(QueryPacket... packetToServer) {
        QueryPacket packetFromServer = null;
        try {
            InetAddress hostIp = InetAddress.getByName(EncProvider.serverHostname);

            Socket serverSocket = new Socket(hostIp, EncProvider.serverPort);

            Log.i("EncProvider", "Connecting to Server with Packet type: " + packetToServer[0].type);

            ObjectOutputStream out = new ObjectOutputStream(serverSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(serverSocket.getInputStream());

            out.writeObject(packetToServer[0]);

            packetFromServer = (QueryPacket) in.readObject();

            serverSocket.close();
            out.close();
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return packetFromServer;
    }
}
