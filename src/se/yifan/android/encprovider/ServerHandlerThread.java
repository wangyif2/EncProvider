package se.yifan.android.encprovider;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import java.io.*;
import java.net.Socket;

/**
 * User: robert
 * Date: 13/01/13
 */
public class ServerHandlerThread extends Thread {
    private Socket socket;

    public ServerHandlerThread(Socket accept) {
        super("ServerHandlerThread");
        this.socket = accept;
        System.out.println("Created new Thread to handle client");
    }

    @Override
    public void run() {
        try {
            QueryPacket fromClient;

            ObjectInputStream from = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream to = new ObjectOutputStream(socket.getOutputStream());

            while ((fromClient = (QueryPacket) from.readObject()) != null) {
                QueryPacket toClient = new QueryPacket();

                /* process message */
                switch (fromClient.type) {
                    case QueryPacket.DB_CREATE:
                        createDB(fromClient);
                        break;
                    case QueryPacket.DB_INSERT:
                        break;
                    case QueryPacket.DB_QUERY:
                        break;
                    case QueryPacket.DB_UPDATE:
                        break;
                    case QueryPacket.DB_DELETE:
                        break;
                    case QueryPacket.DB_NULL:
                        break;
                    default:
                        break;
                }
            }

            System.err.println("ERROR: Unknown ECHO_* packet!!");
            System.exit(-1);

            from.close();
            to.close();
            socket.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (OptionalDataException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    private void createDB(QueryPacket fromClient) throws SQLiteException {
        System.out.println("Trying to Create Local Database with name: " + fromClient.db_name + "\n"
                + "and Creation Statement: \n" + fromClient.db_creation);

        SQLiteConnection db = new SQLiteConnection(new File(fromClient.db_name));
        db.open(true);

        db.exec(fromClient.db_creation);
        db.exec("INSERT INTO contacts VALUES ('0','john','john@gmail.com',5)");

        SQLiteStatement sqLiteStatement = db.prepare("SELECT * FROM contacts");

//        sqLiteStatement.bind(0,"name");
        while (sqLiteStatement.step()) {

            System.out.println("Entry in Database:\nName: " + sqLiteStatement.columnString(1) + "\nEmail: " + sqLiteStatement.columnString(2));
        }

        db.dispose();
    }
}