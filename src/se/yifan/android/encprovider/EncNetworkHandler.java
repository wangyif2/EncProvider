package se.yifan.android.encprovider;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

/**
 * User: robert
 * Date: 26/01/13
 */
public class EncNetworkHandler extends AsyncTask<QueryPacket, Integer, QueryPacket> {
    long startTime,endTime;
    @Override
    protected QueryPacket doInBackground(QueryPacket... packetToServer) {
        QueryPacket packetFromServer = null;
        try {
//            Log.i("EncProvider", "Connecting to Server with Packet type: " + packetToServer[0].type);

            startTime = System.currentTimeMillis();
            EncProvider.out.writeObject(packetToServer[0]);
            endTime = System.currentTimeMillis();
            Log.i("EncProvider-timeLog", "Time spent sending " + (endTime-startTime));

            startTime = System.currentTimeMillis();
            packetFromServer = (QueryPacket) EncProvider.in.readObject();
            endTime = System.currentTimeMillis();
            Log.i("EncProvider-timeLog", "Time spent waiting for receive " + (endTime-startTime));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return packetFromServer;
    }
}
