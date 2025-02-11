package server.services;

import common.SerializableImage;
import common.Utils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
import server.ServerConstants;
import server.services.SQLService.Transaction;

/**
 * Stores users and their related attributes
 */
public final class UserService {
    
    /**
     * Contains methods to be run on the deletion of users
     */
    private static final ConcurrentHashMap<Integer, ArrayList<Runnable>> deletionSubscribers = new ConcurrentHashMap<>();
    /**
     * Protects access to {@link #deletionSubscribers}
     */
    private static final ReentrantReadWriteLock deletionSubscribersLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    
    /**
     * Registers the given method to be run if the specified user is deleted
     * @param userId the userId of the user to check
     * @param onUserDeletion the method to be run
     * @throws IllegalArgumentException if the specified user does not exist
     * @throws SQLException if there was an error accessing the SQL server
     */
    public static void subscribeToDeletionEvents(int userId, Runnable onUserDeletion) throws IllegalArgumentException, SQLException {
        deletionSubscribersLock.readLock().lock();
        try {
            if(!checkUserIdExistance(userId)) {
                throw new IllegalArgumentException("User: " + userId + " does not exist");
            }
            synchronized(deletionSubscribers) {
                if(!deletionSubscribers.containsKey(userId)) {
                    deletionSubscribers.put(userId, new ArrayList<>());
                }
            }
            synchronized(deletionSubscribers.get(userId)) {
                deletionSubscribers.get(userId).add(onUserDeletion);
            }
        } finally {
            deletionSubscribersLock.readLock().unlock();
        }
    }
    
    /**
     * Unregisters the given method from being run when the specified user is deleted
     * @param userId the userId of the user being checked
     * @param callback the method which would be run
     */
    public static void unsubscribeFromDeletionEvents(int userId, Runnable callback) {
        deletionSubscribersLock.readLock().lock();
        try {
            synchronized(deletionSubscribers) {
                if(!deletionSubscribers.containsKey(userId)) {
                    return;
                }
            }
            synchronized(deletionSubscribers.get(userId)) {
                deletionSubscribers.get(userId).remove(callback);
            }
        } finally {
            deletionSubscribersLock.readLock().unlock();
        }
    }
    
    /**
     * Checks if the given userid corresponds to an existing user
     * @param userId the userid to check
     * @return whether the given userid corresponds to an existing user
     * @throws SQLException if there was an error accessing the SQL server
     */
    public static boolean checkUserIdExistance(int userId) throws SQLException {
        if(userId == -1) {
            return false;
        }
        try(Transaction transaction = SQLService.startTransaction(Connection.TRANSACTION_READ_COMMITTED, true)) {
            Connection conn = SQLService.getConnection();
            try(PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE id=?")) {
                stmt.setInt(1, userId);
                try(ResultSet result = stmt.executeQuery()) {
                    return result.getInt(1) != 0;
                }
            }
        }
    }
    
    /**
     * Checks if the given username corresponds to an existing user
     * @param username the username to check
     * @return whether the given user is registered
     * @throws SQLException if there was an error accessing the SQL server
     */
    public static boolean checkUserExistance(String username) throws SQLException {
        if(username == null) {
            return false;
        }
        try(Transaction transaction = SQLService.startTransaction(Connection.TRANSACTION_READ_COMMITTED, true)) {
            Connection conn = SQLService.getConnection();
            try(PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username=?")) {
                stmt.setString(1, username);
                try(ResultSet result = stmt.executeQuery()) {
                    return result.getInt(1) != 0;
                }
            }
        }
    }
    
    /**
     * Registers the given user
     * @param username the username of the user to create
     * @param password the password of the user to create
     * @throws IllegalArgumentException if the userId of the given user is invalid or already registered
     * @throws SQLException if there was an error accessing the SQL server
     */
    public static void createUser(String username, String password) throws IllegalArgumentException, SQLException {
        if(isInvalidUsername(username)) {
            throw new IllegalArgumentException("Username Cannot Contain Any of the Following Characters: " + ServerConstants.USERNAME_FORBIDDEN_CHARACTERS);
        }
        try(Transaction transaction = SQLService.startTransaction(Connection.TRANSACTION_SERIALIZABLE, false)) {
            if (checkUserExistance(username)) {
                throw new IllegalArgumentException("User Already Exists");
            }
            Connection conn = SQLService.getConnection();
            String salt = Utils.randomString(8);
            try(PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password, salt) VALUES (?, ?, ?)")) {
                stmt.setString(1, username);
                stmt.setBytes(2, Utils.hash((password + salt).getBytes()));
                stmt.setString(3, salt);
                stmt.executeUpdate();
            }
            transaction.commit();
        }
    }
    
    /**
     * Deletes the specified user
     * @param username the username of the user to delete
     * @throws IllegalArgumentException if the specified user does not exist
     * @throws SQLException if there was an error accessing the SQL server
     * @throws SecurityException if the current user does not have the required permissions to delete the specified user
     */
    public static void deleteUser(String username) throws IllegalArgumentException, SecurityException, SQLException {
        try(Transaction transaction = SQLService.startTransaction(Connection.TRANSACTION_SERIALIZABLE, false)) {
            if(!checkUserExistance(username)) {
                throw new IllegalArgumentException("User Does Not Exist");
            }
            LoginService.checkAccess(getUserId(username));
            Connection conn = SQLService.getConnection();
            try(PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE username=?")) {
                stmt.setString(1, username);
                stmt.executeUpdate();
            }
            transaction.commit();
        }
    }
    
    /**
     * Retrieves the userId corresponding to the specified username
     * @param username the username to fetch the id for
     * @return the id of the user corresponding to the specified username
     * @throws IllegalArgumentException if the specified user does not exist
     * @throws SecurityException if the current user does not have the permissions to fetch this user's id
     * @throws SQLException if there was an error accessing the SQL server
     */ 
    public static int getUserId(String username) throws IllegalArgumentException, SecurityException, SQLException {
        try(Transaction transaction = SQLService.startTransaction(Connection.TRANSACTION_READ_COMMITTED, false)) {
            if(!checkUserExistance(username)) {
                throw new IllegalArgumentException("User Does Not Exist");
            }
            Connection conn = SQLService.getConnection();
            try(PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username=?")) {
                stmt.setString(1, username);
                try(ResultSet result = stmt.executeQuery()) {
                    int id = result.getInt(1);
                    LoginService.checkAccess(id);
                    return id;
                }
            }
        }
    }
    
    /**
     * Retrieves the username corresponding to the specified userId
     * @param userId the userId to fetch the username for
     * @return the username of the user corresponding to the specified userId
     * @throws IllegalArgumentException if the specified user does not exist
     * @throws SecurityException if the current user does not have the permissions to fetch this user's username
     * @throws SQLException if there was an error accessing the SQL server
     */
    public static String getUsername(int userId) throws IllegalArgumentException, SecurityException, SQLException {
        LoginService.checkAccess(userId);
        try(Transaction transaction = SQLService.startTransaction(Connection.TRANSACTION_READ_COMMITTED, false)) {
            if(!checkUserIdExistance(userId)) {
                throw new IllegalArgumentException("User Does Not Exist");
            }
            Connection conn = SQLService.getConnection();
            try(PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE id=?")) {
                stmt.setInt(1, userId);
                try(ResultSet result = stmt.executeQuery()) {
                    return result.getString(1);
                }
            }
        }
    }
    
    /**
     * Not Implemented Yet
     * @param userId
     * @return 
     * throws IllegalArgumentException
     * @throws SecurityException
     * @throws SQLException
     */
    public static SerializableImage getUserIcon(int userId) throws IllegalArgumentException, SecurityException, SQLException {
        LoginService.checkAccess(userId);
        throw new UnsupportedOperationException("Not Implemented Yet.");
    }
    
    /**
     * A pattern which matches to any invalid username
     */
    public static final Pattern INVALID_USERNAME_PATTERN = Pattern.compile(ServerConstants.USERNAME_FORBIDDEN_CHARACTERS_REGEX);
    /**
     * Checks if the given username is invalid
     * @param username the username to check
     * @return whether the given username is invalid
     */
    public static boolean isInvalidUsername(String username) {
        return INVALID_USERNAME_PATTERN.matcher(username).find();
    }
    
    
    private UserService() {}

}