package se.yifan.android.encprovider;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.sql.Statement;
import java.util.HashMap;

/**
 * User: robert
 * Date: 13/01/13
 */
public class ServerHandlerThread extends Thread {
//    Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.OFF);
    private Socket socket;
    SQLiteConnection db;
    Gson gson = new Gson();
    private boolean done = false;
    final static Logger logger = LoggerFactory.getLogger(ServerHandlerThread.class);

    public ServerHandlerThread(Socket accept) {
        super("ServerHandlerThread");
        this.socket = accept;
        logger.info("New ServerhandlerThread created...");
    }

    @Override
    public void run() {
        try {
            QueryPacket fromClient;

            ObjectInputStream from = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream to = new ObjectOutputStream(socket.getOutputStream());

            while (!done && (fromClient = (QueryPacket) from.readObject()) != null) {
                QueryPacket toClient = new QueryPacket();
                logger.info("Packet Type: " + fromClient.type);
                Server.dbName = fromClient.db_name;

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
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private QueryPacket insertDB(QueryPacket fromClient) throws SQLiteException, ParseException, JSONException {
        db = getDb();
        Statement  s;

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(fromClient.table);
        sql.append('(');

        JSONObject json = (JSONObject) new JSONParser().parse(fromClient.contentValues);
        HashMap<String,String> contentValues = (HashMap<String,String>) json.get("mValues");
        Object[] bindArgs = null;

        int size = (contentValues != null && contentValues.size() > 0) ? contentValues.size() : 0;
        if (size > 0) {
            bindArgs = new Object[size];
            int i = 0;
            for (String colName : contentValues.keySet()) {
                sql.append((i > 0) ? "," : "");
                sql.append(colName);
                bindArgs[i++] = contentValues.get(colName);
            }
            sql.append(')');
            sql.append(" VALUES (");
            for (i = 0; i < size; i++) {
                sql.append((i > 0) ? "," + bindArgs[i].toString() : bindArgs[i].toString());
            }
        } else {
            sql.append(fromClient.nullColumnHack).append(") VALUES (NULL");
        }
        sql.append(')');

        logger.info("Insert: " + sql.toString());

//        db.exec(sql.toString());

        db.dispose();

        QueryPacket toClient = new QueryPacket();
        toClient.key = "Test";

        return toClient;
    }

    private QueryPacket createDB(QueryPacket fromClient) throws SQLiteException {

        logger.info("Create: Database name: " + fromClient.db_name + "\n"
                + "and Creation Statement: \n" + fromClient.db_creation);


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