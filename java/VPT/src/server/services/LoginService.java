package server.services;

import server.user.User;
import server.user.UserStore;

/**
 * Manages logins
 */
public final class LoginService {
    
    /**
     * Stores the current thread's session
     */
    private static final ThreadLocal<Session> session = ThreadLocal.withInitial(() -> new Session());
    /**
     * Stores whether the current thread is managed by the system and should run with administrator privileges
     */
    private static final ThreadLocal<Boolean> isSystemThread = ThreadLocal.withInitial(() -> false);
    
    /**
     * Logs in the current thread
     * @param userId the requested userId to login
     * @param password the password of the requested user
     * @return whether the login was successful
     */
    public static boolean login(String userId, byte[] password) {
        User user = UserStore.login(userId, password);
        if(user != null) {
            session.get().setUser(user);
            return true;
        }
        return false;
    }
    
    /**
     * Logs out the current thread
     */
    public static void logout() {
        session.get().setUser(-1);
    }
    
    /**
     * Retrieves the user logged in to the current thread or <code>null</code> if the current thread is logged out
     * @return The user logged in to the current thread
     */
    public static int getCurrentUserId() {
        return session.get().getUserId();
    }
    
    /**
     * Checks if the currently logged in user is an administrator
     * @throws SecurityException if the currently logged in user is not an administrator
     */
    public static void checkAccess() throws SecurityException {
        if(isSystemThread.get()) {
            return;
        }
        if(getCurrentUserId() != -1 /* && isAdmin */) {
            return;
        }
        throw new SecurityException("Invalid Permissions");
    }
    
    /**
     * Checks if the currently logged in user can modify the properties of the given user
     * @param userId the userId to check
     * @throws SecurityException if the currently logged in user cannot modify the properties of the given user
     */
    public static void checkAccess(int userId) throws SecurityException {
        if(userId == -1) {
            throw new SecurityException("Invalid Permissions");
        }
        if(getCurrentUserId() == userId) {
            return;
        }
        checkAccess();
    }
    
    /**
     * Marks the current thread as being managed by the system and gives it administrator privileges.
     * This should ONLY be called from <code>server.ServerMain</code>
     * @throws RuntimeException If the invoking class is not <code>server.ServerMain</code>
     */
    public static void markAsSystemThread() throws RuntimeException {
        if(!Thread.currentThread().getStackTrace()[2].getClassName().equals("server.ServerMain")) {
            //This shouldn't occur. Throw RuntimeException and crash the thread
            throw new RuntimeException();
        }
        isSystemThread.set(true);
    }
    
    /**
     * Manages the logged in status a given thread
     */
    private static final class Session {
        
        /**
         * The user logged into a this Session
         */
        private int userId;
        /**
         * A reference to {@link #onUserDeletion()}
         */
        private Runnable onUserDeletion;

        /**
         * Creates a new Session and initializes all of its variables to <code>null</code>
         */
        private Session() {
            userId = -1;
            onUserDeletion = null;
        }
        
        /**
         * Sets the user associated with this Session
         * @param user the User to associate with this Session
         */
        private synchronized void setUser(int userId) {
            if(onUserDeletion == null) {
                onUserDeletion = this::onUserDeletion;
            }
            if(this.userId != -1) {
                DeletionService.unsubscribeFromDeletionEvents(this.userId, onUserDeletion);
            }
            this.userId = userId;
            if(userId != -1) {
                try {
                    DeletionService.subscribeToDeletionEvents(userId, onUserDeletion);
                } catch(IllegalArgumentException e) {
                    this.userId = -1;
                }
            }
        }
        
        /**
         * Retrieves the user currently associated with this Session
         * @return the user currently associated with this Session
         */
        private synchronized int getUserId() {
            return userId;
        }
        
        /**
         * This method is called by {@link UserStore} when the user currently associated with this Session is deleted
         */
        private synchronized void onUserDeletion() {
            this.userId = -1;
        }
        
    }
    
    private LoginService() {}
    
}