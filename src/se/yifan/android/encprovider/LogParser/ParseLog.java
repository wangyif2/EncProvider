package se.yifan.android.encprovider.LogParser;

import java.io.*;
import java.nio.charset.Charset;

/**
 * User: robert
 * Date: 26/03/13
 */
public class ParseLog {
    static InputStream clientFile;
    static InputStream serverFile;
    static BufferedReader clientBr, serverBr;
    static String line, lineServer;

    public static void main(String[] args) throws IOException {
        clientFile = new FileInputStream("/home/robert/etc/enclog_client.log");
        serverFile = new FileInputStream("/home/robert/etc/enclog_server.log");

        FileOutputStream finalFile = new FileOutputStream("/home/robert/etc/enclog_final.log");
        BufferedWriter finalBR = new BufferedWriter(new OutputStreamWriter(finalFile, Charset.forName("UTF-8")));

        clientBr = new BufferedReader(new InputStreamReader(clientFile, Charset.forName("UTF-8")));
        serverBr = new BufferedReader(new InputStreamReader(serverFile, Charset.forName("UTF-8")));
        while ((line = clientBr.readLine()) != null) {
            finalBR.write(line);
            finalBR.newLine();

            if (line.contains("sendToServer")) {
                do {
                    lineServer = serverBr.readLine();
                    finalBR.write(lineServer);
                    finalBR.newLine();
                } while (!lineServer.contains("run-end"));
                finalBR.write(lineServer);
                finalBR.newLine();
            } else if (line.contains("contactProvider-end:")) {
                finalBR.newLine();
                finalBR.newLine();
            }
        }
    }
}
