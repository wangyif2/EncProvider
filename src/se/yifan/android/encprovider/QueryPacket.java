package se.yifan.android.encprovider;

import java.io.Serializable;

/**
 * User: robert
 * Date: 13/01/13
 */
public class QueryPacket implements Serializable{
    public static final int DB_NULL = 0;
    public static final int DB_CREATE = 100;
    public static final int DB_QUERY = 101;
    public static final int DB_INSERT = 102;
    public static final int DB_DELETE = 103;
    public static final int DB_UPDATE = 104;

    public static final int ERROR_INVALID_SYMBOL = -101;
    public static final int ERROR_OUT_OF_RANGE = -102;
    public static final int ERROR_SYMBOL_EXISTS = -103;
    public static final int ERROR_INVALID_EXCHANGE = -104;

    public int type = QueryPacket.DB_NULL;

    public String db_name;
    public String db_creation;

    public String db_exec;
    public String db_query;

    @Override
    public String toString() {
        return "QueryPacket{" +
                "db_name='" + db_name + '\'' +
                ", db_creation='" + db_creation + '\'' +
                ", db_exec='" + db_exec + '\'' +
                ", db_query='" + db_query + '\'' +
                '}';
    }
}
