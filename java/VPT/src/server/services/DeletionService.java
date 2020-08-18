package server.services;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import server.ServerConstants;
import server.user.UserStore;

public final class DeletionService {
    
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
     */
    public static void subscribeToDeletionEvents(int userId, Runnable onUserDeletion) throws IllegalArgumentException {
        deletionSubscribersLock.readLock().lock();
        try {
            if(!UserStore.checkUserIdExistance(userId)) {
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
    
}