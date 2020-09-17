package server.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import server.ServerConstants;

/**
 * Handles connections to the backend SQL server
 */
public final class SQLService {
    
    private static final ThreadLocal<Connection> connectionTL = ThreadLocal.withInitial(SQLService::createConnection);
    private static String sqlServerPassword = null;
    
    /**
     * Sets the password to use to access the SQL server
     * @param password the password to use
     * @throws IllegalStateException if the password has already been set
     */
    public static void setSQLServerPassword(String password) throws IllegalStateException {
        if(sqlServerPassword != null) {
            throw new IllegalStateException("SQL Server Password Already Set");
        }
        sqlServerPassword = password;
    }
    
    /**
     * Sets the properties which will be used for the next SQL transaction
     * @param transactionIsolation the transaction isolation for the transaction
     * @param isReadOnly whether the transaction is readonly
     * @throws SQLException if there was an error accessing the SQL server
     * @see Connection#TRANSACTION_READ_UNCOMMITTED
     * @see Connection#TRANSACTION_READ_COMMITTED
     * @see Connection#TRANSACTION_REPEATABLE_READ
     * @see Connection#TRANSACTION_SERIALIZABLE
     */
    public static void setTransactionProperties(int transactionIsolation, boolean isReadOnly) throws SQLException {
        Connection conn = getConnection();
        conn.setTransactionIsolation(transactionIsolation);
        conn.setReadOnly(isReadOnly);
    }
    
    /**
     * Gets the Connection to the SQL server associated with this {@link Thread} and creates one if it does not already exist
     * @return a Connection to the SQL server associated with this {@link Thread}
     */
    public static Connection getConnection() {
        return connectionTL.get();
    }
    
    /**
     * Starts an SQL transaction which will, by default, call {@link Transaction#rollback()} on close 
     * @return a {@link Transaction} representing the SQL transaction which will, by default, rollback on close
     * @throws SQLException if there was an error accessing the SQL server
     * @see #startTransaction(boolean) 
     * @see #startTransaction(int, boolean) 
     * @see #startTransaction(int, boolean, boolean) 
     */
    public static Transaction startTransaction() throws SQLException {
        return startTransaction(false);
    }
    
    /**
     * Starts an SQL transaction
     * @param commitOnClose whether the transaction will, by default, commit or rollback when closed
     * @return a {@link Transaction} representing the SQL transaction
     * @throws SQLException if there was an error accessing the SQL server
     * @see #startTransaction() 
     * @see #startTransaction(int, boolean) 
     * @see #startTransaction(int, boolean, boolean) 
     */
    public static Transaction startTransaction(boolean commitOnClose) throws SQLException {
        return new Transaction(connectionTL.get(), commitOnClose);
    }
    
    /**
     * Starts an SQL transaction with the specified parameters which will, by default, call {@link Transaction#rollback()} on close
     * @param transactionIsolation the transaction isolation for the transaction
     * @param isReadOnly whether the transaction is readonly
     * @return a {@link Transaction} representing the SQL transaction which will, by default, rollback on close
     * @throws SQLException if there was an error accessing the SQL server
     * @see #setTransactionProperties(int, boolean) 
     * @see #startTransaction() 
     * @see #startTransaction(boolean) 
     * @see #startTransaction(int, boolean, boolean) 
     */
    public static Transaction startTransaction(int transactionIsolation, boolean isReadOnly) throws SQLException {
        setTransactionProperties(transactionIsolation, isReadOnly);
        return startTransaction();
    }
    
    /**
     * Starts an SQL transaction
     * @param transactionIsolation the transaction isolation for the transaction
     * @param isReadOnly whether the transaction is readonly
     * @param commitOnClose whether the transaction will, by default, commit or rollback when closed
     * @return a {@link Transaction} representing the SQL transaction
     * @throws SQLException if there was an error accessing the SQL server
     * @see #setTransactionProperties(int, boolean) 
     * @see #startTransaction() 
     * @see #startTransaction(boolean) 
     * @see #startTransaction(int, boolean) 
     */
    public static Transaction startTransaction(int transactionIsolation, boolean isReadOnly, boolean commitOnClose) throws SQLException {
        setTransactionProperties(transactionIsolation, isReadOnly);
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
    
    /**
     * Represents an SQL transaction
     */
    public static final class Transaction implements AutoCloseable {

        private final Connection conn;
        private final boolean commitOnClose;
        private boolean isOpen;
        
        /**
         * Creates a new SQL transaction
         * @param conn the Connection on which the transaction is running
         * @param commitOnClose whether the transaction will, by default, commit or rollback when closed
         * @throws SQLException if there was an error accessing the SQL server
         */
        public Transaction(Connection conn, boolean commitOnClose) throws SQLException {
            this.conn = conn;
            this.commitOnClose = commitOnClose;
            conn.setAutoCommit(false);
            isOpen = true;
        }
        
        /**
         * @return is this Transaction still open
         */
        public boolean isOpen() {
            return isOpen;
        }
        
        /**
         * Commits this Transaction
         * @throws SQLException if there was an error accessing the SQL server
         */
        public void commit() throws SQLException {
            if(isOpen) {
                conn.commit();
                conn.setAutoCommit(true);
                isOpen = false;
            }
        }
        
        /**
         * Rollsback this Transaction
         * @throws SQLException if there was an error accessing the SQL server
         */
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