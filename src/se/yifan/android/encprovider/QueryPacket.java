package se.yifan.android.encprovider;

import java.io.Serializable;

/**
 * User: robert
 * Date: 13/01/13
 */
public class QueryPacket implements Serializable {
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
    public String table;

    //for create
    public String db_creation;

    //for insert
    public String contentValues;
    public String nullColumnHack;

    //for delete
    public String whereClause;
    //for query
    public String db_query;

    public String key;
    public String[] args;
    public int[] contentType;


    @Override
    public String toString() {
        return "QueryPacket{" +
                "type=" + type +
                ", db_name='" + db_name + '\'' +
                ", db_creation='" + db_creation + '\'' +

                ", contentValues='" + contentValues + '\'' +
                ", db_query='" + db_query + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
