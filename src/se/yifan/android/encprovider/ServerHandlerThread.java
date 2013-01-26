package se.yifan.android.encprovider;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

/**
 * User: robert
 * Date: 13/01/13
 */
public class ServerHandlerThread extends Thread {
    private Socket socket;
    SQLiteConnection db;
    Gson gson = new Gson();
    private boolean done = false;

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

            while (!done && (fromClient = (QueryPacket) from.readObject()) != null) {
                QueryPacket toClient = new QueryPacket();

                /* process message */
                switch (fromClient.type) {
                    case QueryPacket.DB_CREATE:
                        toClient = createDB(fromClient);
                        to.writeObject(toClient);
                        done = true;
                        break;
                    case QueryPacket.DB_INSERT:
                        toClient = insertDB(fromClient);
                        to.writeObject(toClient);
                        done = true;
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

    private QueryPacket insertDB(QueryPacket fromClient) {
        db = getDb();

        System.out.println("Server tring to Insert");
        System.out.println(fromClient.uri + fromClient.contentValues);
//        Uri uri = Uri.parse(fromClient.uri);
//        ContentValues contentValues = gson.fromJson(fromClient.contentValues, ContentValues.class);

        db.dispose();

        QueryPacket toClient = new QueryPacket();
        toClient.key = "Test";

        return toClient;
    }

    private QueryPacket createDB(QueryPacket fromClient) throws SQLiteException {
        System.out.println("Trying to Create Local Database with name: " + fromClient.db_name + "\n"
                + "and Creation Statement: \n" + fromClient.db_creation);

        Server.dbName = fromClient.db_name;
        File dbFile = new File(Server.dbName);

        if (dbFile.exists())
            return fromClient;

        db = getDb();
        db.open(true).exec(fromClient.db_creation);
        db.dispose();

        QueryPacket toClient = new QueryPacket();
        toClient.key = "Test";

        return toClient;
    }

    private SQLiteConnection getDb() {
        if (db == null)
            db = new SQLiteConnection(new File(Server.dbName));
        return db;
    }

//        db.exec("INSERT INTO contacts VALUES ('0','john','john@gmail.com',5)");

//        SQLiteStatement sqLiteStatement = db.prepare("SELECT * FROM contacts");
//
////        sqLiteStatement.bind(0,"name");
//        while (sqLiteStatement.step()) {
//
//            System.out.println("Entry in Database:\nName: " + sqLiteStatement.columnString(1) + "\nEmail: " + sqLiteStatement.columnString(2));
//        }
}