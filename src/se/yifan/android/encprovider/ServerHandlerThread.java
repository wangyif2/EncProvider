package se.yifan.android.encprovider;

import android.database.Cursor;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.HashMap;

/**
 * User: robert
 * Date: 13/01/13
 */
public class ServerHandlerThread extends Thread {
    //    Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.OFF);
    private Socket socket;
    SQLiteConnection db;
    private boolean done = false;
    Connection connection = null;
    final static Logger logger = LoggerFactory.getLogger(ServerHandlerThread.class);
    final static String dbConnectionString = "jdbc:sqlite:/home/robert/project/IDEA/EncProvider/contacts.db";

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
                logger.info("Packet Type: " + fromClient.type);
                Server.dbName = fromClient.db_name;

                /* process message */
                switch (fromClient.type) {
                    case QueryPacket.DB_CREATE:
                        to.writeObject(createDB(fromClient));
                        done = true;
                        break;
                    case QueryPacket.DB_INSERT:
                        to.writeObject(insertDB(fromClient));
                        done = true;
                        break;
                    case QueryPacket.DB_QUERY:
                        to.writeObject(queryDB(fromClient));
                        done = true;
                        break;
                    case QueryPacket.DB_UPDATE:
                        break;
                    case QueryPacket.DB_DELETE:
                        to.writeObject(deleteDB(fromClient));
                        done = true;
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
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.info("done");
    }

    private QueryPacket createDB(QueryPacket fromClient) throws SQLException {

        logger.info("Create: Database name: " + fromClient.db_name + "\nand Creation Statement:\n" + fromClient.db_creation);

        connection = DriverManager.getConnection(dbConnectionString);
        Statement sql = connection.createStatement();
        sql.execute(fromClient.db_creation);
        sql.close();
        connection.close();

        QueryPacket toClient = new QueryPacket();
        toClient.key = "Test";

        return toClient;
    }

    private QueryPacket queryDB(QueryPacket fromClient) throws SQLException {
        String sql = fromClient.db_query;
        String[] sqlArgs = fromClient.args;

        connection = DriverManager.getConnection(dbConnectionString);
        PreparedStatement preparedStatement = buildQuerySql(sql, sqlArgs);

        logger.info("Query: " + preparedStatement.toString());

        ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++)
                logger.info(resultSetMetaData.getColumnName(i) + ": " + resultSet.getString(i));
        }

        resultSet.close();
        preparedStatement.close();
        connection.close();

        QueryPacket toClient = new QueryPacket();
        toClient.key = "Test";

        return toClient;
    }

    private QueryPacket insertDB(QueryPacket fromClient) throws ParseException, SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(fromClient.table);
        sql.append(" (");

        JSONObject json = (JSONObject) new JSONParser().parse(fromClient.contentValues);
        HashMap<String, String> contentValues = (HashMap<String, String>) json.get("mValues");
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
//            for (i = 0; i < size; i++) {
//                sql.append((i > 0) ? "," + bindArgs[i].toString() : bindArgs[i].toString());
//            }
            sql = bindArguments(sql, bindArgs, fromClient.contentType);
        } else {
            sql.append(fromClient.nullColumnHack).append(") VALUES (NULL");
        }
        sql.append(')');

        logger.info("Insert: " + sql.toString());
//        connection = DriverManager.getConnection(dbConnectionString);
//        Statement sqlStatement = connection.createStatement();
//
//        sqlStatement.execute(sql.toString());
//
//        sqlStatement.close();
//        connection.close();

        synchronized (this) {
            try {
                db = getDb();
                db.open();
                db.exec(sql.toString());
                db.dispose();
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }

        QueryPacket toClient = new QueryPacket();
        toClient.key = "Test";

        return toClient;
    }

    private QueryPacket deleteDB(QueryPacket fromClient) throws SQLException {
        String whereClause = fromClient.whereClause;
        String[] whereArgs = fromClient.args;

        connection = DriverManager.getConnection(dbConnectionString);

        PreparedStatement preparedStatement = buildQuerySql("DELETE FROM " + fromClient.table +
                ((whereClause != null) ? " WHERE " + whereClause : ""), whereArgs);

        logger.info("Delete: " + preparedStatement.toString());

        preparedStatement.executeUpdate();

        preparedStatement.close();
        connection.close();

        QueryPacket toClient = new QueryPacket();
        toClient.key = "Test";

        return toClient;
    }

    private PreparedStatement buildQuerySql(String sql, String[] sqlArgs) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if (sqlArgs != null) {
            int i = 0;
            for (String args : sqlArgs) {
                preparedStatement.setObject(i++, args);
            }
        }
        return preparedStatement;
    }

    private StringBuilder bindArguments(StringBuilder sql, Object[] bindArgs, int[] bindArgsType) {
        final int count = bindArgs != null ? bindArgs.length : 0;

        if (count == 0) {
            return sql;
        }

        for (int i = 0; i < count; i++) {
            final Object arg = bindArgs[i];
            switch (bindArgsType[i]) {
                case Cursor.FIELD_TYPE_NULL:
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    sql.append((i > 0) ? "," + ((Number) arg).longValue() : ((Number) arg).longValue());
                    break;
                case Cursor.FIELD_TYPE_STRING:
                default:
                    sql.append((i > 0) ? ", \"" + arg.toString() + "\"" : "\"" + arg.toString() + "\"");
                    break;
            }
        }
        return sql;
    }

    private SQLiteConnection getDb() {
        if (db == null)
            db = new SQLiteConnection(new File(Server.dbName));
        return db;
    }

}