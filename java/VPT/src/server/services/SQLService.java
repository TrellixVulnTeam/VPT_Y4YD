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
    
    public static void setTransactionProperties(int transactioIsolation, boolean isReadOnly) throws SQLException {
        Connection conn = getConnection();
        conn.setTransactionIsolation(transactioIsolation);
        conn.setReadOnly(isReadOnly);
    }
    
    public static Connection getConnection() {
        return connectionTL.get();
    }
    
    public static Transaction startTransaction() throws SQLException {
        return startTransaction(false);
    }
    
    public static Transaction startTransaction(boolean commitOnClose) throws SQLException {
        return new Transaction(connectionTL.get(), commitOnClose);
    }
    
    public static Transaction startTransaction(int transactioIsolation, boolean isReadOnly) throws SQLException {
        setTransactionProperties(transactioIsolation, isReadOnly);
        return startTransaction();
    }
    
    public static Transaction startTransaction(int transactioIsolation, boolean isReadOnly, boolean commitOnClose) throws SQLException {
        setTransactionProperties(transactioIsolation, isReadOnly);
        return startTransaction(commitOnClose);
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
    
    public static final class Transaction implements AutoCloseable {

        private final Connection conn;
        private final boolean commitOnClose;
        private boolean isOpen;
        
        public Transaction(Connection conn, boolean commitOnClose) throws SQLException {
            this.conn = conn;
            this.commitOnClose = commitOnClose;
            conn.setAutoCommit(false);
            isOpen = true;
        }
        
        public boolean isOpen() {
            return isOpen;
        }
        
        public void commit() throws SQLException {
            if(isOpen) {
                conn.commit();
                conn.setAutoCommit(true);
                isOpen = false;
            }
        }
        
        public void rollback() throws SQLException {
            if(isOpen) {
                conn.rollback();
                conn.setAutoCommit(true);
                isOpen = false;
            }
        }
        
        @Override
        public void close() throws SQLException {
            if(commitOnClose) {
                commit();
            } else {
                rollback();
            }
        }
        
    }
    
}