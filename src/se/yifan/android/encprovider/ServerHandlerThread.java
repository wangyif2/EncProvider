package se.yifan.android.encprovider;

import android.database.Cursor;
import com.almworks.sqlite4java.SQLiteConnection;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.*;

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

            while ((fromClient = (QueryPacket) from.readObject()) != null && !done) {
                logger.info("Started...\n\tRead Packet Type: " + fromClient.type);
                Server.dbName = fromClient.db_name;

                long startTime = System.currentTimeMillis();
                /* process message */
                switch (fromClient.type) {
                    case QueryPacket.DB_CREATE:
                        to.writeObject(createDB(fromClient));
                        break;
                    case QueryPacket.DB_INSERT:
                        to.writeObject(insertDB(fromClient));
                        break;
                    case QueryPacket.DB_QUERY:
                        to.writeObject(queryDB(fromClient));
                        break;
                    case QueryPacket.DB_UPDATE:
                        break;
                    case QueryPacket.DB_DELETE:
                        to.writeObject(deleteDB(fromClient));
                        break;
                    case QueryPacket.DB_CLOSE:
                        done = true;
                        break;
                    case QueryPacket.DB_NULL:
                        break;
                    default:
                        break;
                }
                long endTime = System.currentTimeMillis();
                logger.info("Finished...\n\tTime spent processing the switch: " + (endTime - startTime));
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
        //start time measure
        long startTime = System.currentTimeMillis();
        logger.info("Create: started.. \n\tDatabase name: " + fromClient.db_name + "\n\tand Creation Statement:\n\t" + fromClient.db_creation);

        //start creating table
        connection = DatabaseConnection.getInstance();
        Statement sql = connection.createStatement();
        sql.execute(fromClient.db_creation);
        sql.close();

        //might not need to reply here
        QueryPacket toClient = new QueryPacket();
        toClient.key = "Test";

        //log the time
        long endTime = System.currentTimeMillis();
        logger.info("Create: done..." + (endTime - startTime));
        return toClient;
    }

    private QueryPacket queryDB(QueryPacket fromClient) throws SQLException {
        //start time measure
        long startTime = System.currentTimeMillis();
        logger.info("Query: started...");

        //start the query
        String sql = fromClient.db_query;
        String[] sqlArgs = fromClient.args;

        connection = DriverManager.getConnection(dbConnectionString);
        PreparedStatement preparedStatement = buildQuerySql(sql, sqlArgs);


        ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++)
                logger.info(resultSetMetaData.getColumnName(i) + ": " + resultSet.getString(i));
        }

        resultSet.close();
        preparedStatement.close();

        //reply with key
        QueryPacket toClient = new QueryPacket();
        toClient.key = "Test";

        //log end time
        long endTime = System.currentTimeMillis();
        logger.info("Query: done..." + (endTime - startTime));

        return toClient;
    }

    private QueryPacket insertDB(QueryPacket fromClient) throws ParseException, SQLException {
        //start time measure
        long startTime = System.currentTimeMillis();
        logger.info("Insert: started...");

        //start the insert
        StringBuilder sql = buildInsertSql(fromClient);

        connection = DatabaseConnection.getInstance();
        Statement p = connection.createStatement();
        p.execute(sql.toString());

        //reply with key
        QueryPacket toClient = new QueryPacket();
        toClient.key = "Test";

        //log end time
        long endTime = System.currentTimeMillis();
        logger.info("Insert: done..." + (endTime - startTime));

        return toClient;
    }

    private QueryPacket deleteDB(QueryPacket fromClient) throws SQLException {
        //start timing
        long startTime = System.currentTimeMillis();
        logger.info("Delete: started... ");

        //start delete
        String whereClause = fromClient.whereClause;
        String[] whereArgs = fromClient.args;

        connection = DatabaseConnection.getInstance();
        PreparedStatement preparedStatement = buildQuerySql("DELETE FROM " + fromClient.table +
                ((whereClause != null) ? " WHERE " + whereClause : ""), whereArgs);
        preparedStatement.executeUpdate();
        preparedStatement.close();

        //might not need, reply with key
        QueryPacket toClient = new QueryPacket();
        toClient.key = "Test";

        //log time spent
        long endTime = System.currentTimeMillis();
        logger.info("Delete: done..." + (endTime - startTime));
        return toClient;
    }

    private StringBuilder buildInsertSql(QueryPacket fromClient) throws ParseException {
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
            sql = bindArguments(sql, bindArgs, fromClient.contentType);
        } else {
            sql.append(fromClient.nullColumnHack).append(") VALUES (NULL");
        }
        sql.append(')');
        return sql;
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