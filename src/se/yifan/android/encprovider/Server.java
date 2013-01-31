package se.yifan.android.encprovider;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * User: robert
 * Date: 13/01/13
 */
public class Server {
    private static String ipv4;
    public static String dbName;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket serverSocket = null;
        boolean listening = true;
        Class.forName("org.sqlite.JDBC");

        try {
            if (args.length == 1) {
                serverSocket = new ServerSocket(Integer.parseInt(args[0]));
                System.out.println("Server up and running with:\nhostname: " + getLocalIpAddress() + "\nport: " + args[0]);
                System.out.println("Waiting to accept client...");
            } else {
                System.err.println("ERROR: Invalid arguments!");
                System.exit(-1);
            }
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }

        while (listening) {
            new ServerHandlerThread(serverSocket.accept()).start();
        }

        serverSocket.close();
    }

    // gets the ip address of your phone's network
    private static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (!intf.getDisplayName().equals("wlan0")) continue;
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            System.err.println(ex);
        }
        return null;
    }
}
