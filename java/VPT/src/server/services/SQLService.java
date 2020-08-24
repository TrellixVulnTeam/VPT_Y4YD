package server.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import server.ServerConstants;

public final class SQLService {
    
    private static final ThreadLocal<Connection> connectionTL = ThreadLocal.withInitial(SQLService::createConnection);
    private static String sqlServerPassword = null;
    
    public static void setSQLServerPassword(String password) throws IllegalStateException {
        if(sqlServerPassword != null) {
            throw new IllegalStateException("SQL Server Password Already Set");
        }
        sqlServerPassword = password;
    }
   
    private static Connection createConnection() throws IllegalStateException {
        if(sqlServerPassword == null) {
            throw new IllegalStateException("SQL Server Password Unknown");
        }
        try {
            Connection conn = DriverManager.getConnection(ServerConstants.SQL_URL, ServerConstants.SQL_USER, sqlServerPassword);
            //Apply Any Required Connection Changes
            return conn;
        } catch(SQLException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error Connecting to Database: ");
            sb.append(ServerConstants.SQL_URL);
            sb.append("\nMessage: ");
            sb.append(e.getMessage());
            sb.append("\nState: ");
            sb.append(e.getSQLState());
            sb.append("\nError Code: ");
            sb.append(e.getErrorCode());
            System.err.println(sb.toString());
            return null;
        }
    }
    
    private SQLService() {}
    
}