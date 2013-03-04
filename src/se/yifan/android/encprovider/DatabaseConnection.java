package se.yifan.android.encprovider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * User: robert
 * Date: 27/02/13
 */
public class DatabaseConnection {

    private static Connection connection = null;
    private final static String dbConnectionString = "jdbc:sqlite:/home/robert/project/IDEA/EncProvider/contacts.db";

    public static Connection getInstance() {
        if (connection == null)
            try {
                connection = DriverManager.getConnection(dbConnectionString);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return connection;
    }

}
