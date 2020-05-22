package server.user;

import common.Utils;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Contains all the attributes of a User
 */
public class User extends PublicUser {

    private static final long serialVersionUID = -732320744263029685L;
    
    /**
     * The hashed password of this User
     */
    private byte[] password;
    /**
     * Whether this user is publicly visible
     */
    private boolean isVisible;
    /**
     * Whether this user is an administrator
     */
    private boolean isAdmin;
    /**
     * Contains all the keys this User has access to
     */
    private final HashMap<String, Key> keyStore;
    
    /**
     * Creates a new non-admin User with the given userId and password and makes it invisible
     * @param userId the userId to associate with this User
     * @param password the password to associate with this User
     */
    public User(String userId, byte[] password) {
        this(userId, password, false);
    }

    /**
     * Creates a new optionally-admin User with the given userId and password and makes it invisible
     * @param userId the userId to associate with this User
     * @param password the password to associate with this User
     * @param isAdmin the administrator status of this User
     */
    public User(String userId, byte[] password, boolean isAdmin) {
        this(userId, password, isAdmin, false);
    }

    /**
     * Creates a new optionally-admin User with the given userId, password, and visibility
     * @param userId the userId to associate with this User
     * @param password the password to associate with this User
     * @param isAdmin the administrator status of this User
     * @param isVisible this User's visibility status
     */
    public User(String userId, byte[] password, boolean isAdmin, boolean isVisible) {
        this(userId, password, isAdmin, isVisible, new ArrayList<>());
    }

    /**
     * Creates a new with the given information
     * @param userId the userId to associate with this User
     * @param password the password to associate with this User
     * @param isAdmin the administrator status of this User
     * @param isVisible this User's visibility status
     * @param attributes the attributes to associate with this User
     */
    public User(String userId, byte[] password, boolean isAdmin, boolean isVisible, ArrayList<UserAttribute> attributes) {
        super(userId, attributes);
        this.password = Utils.createMD().digest(password);
        this.isVisible = isVisible;
        this.isAdmin = isAdmin;
        this.keyStore = new HashMap<>();
        keyStore.put("USER_FILE_PUBLIC_KEY", Utils.createPasswordKey(password));
    }
    
    /**
     * Checks the given password against this User's password
     * @param password the password to check
     * @return whether the given password matches this User's password
     */
    public boolean checkPassword(byte[] password) {
        return Arrays.equals(this.password, Utils.createMD().digest(password));
    }

    /**
     * Checks whether this user is publicly visible
     * @return whether this user is publicly visible
     * @see #isVisible
     * @see #setVisible(boolean) 
     */
    public boolean isVisible() {
        readWriteLock.readLock().lock();
        try {
            return isVisible;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Sets this user's visibility status
     * @param isVisible this user's new visibility status
     * @throws SecurityException if the currently logged in user does not have the permissions to set this user's visibility status
     * @see #isVisible
     * @see #isVisible() 
     */
    public void setVisible(boolean isVisible) throws SecurityException {
        LoginService.checkAccess(this);
        readWriteLock.writeLock().lock();
        try {
            if(this.isVisible != this.isVisible) {
                this.isVisible = isVisible;
                UserStore.notifyVisibilityChange(this);
                UserStore.notifyUserChange(this);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Checks whether this user is an administrator
     * @return whether this user is an administrator
     * @see #isAdmin
     * @see #setAdmin(boolean) 
     */
    public boolean isAdmin() {
        readWriteLock.readLock().lock();
        try {
            return isAdmin;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Sets this user's administrator status
     * @param isAdmin this user's new administrator status
     * @throws SecurityException if the currently logged in user does not have the permissions to set this user's administrator status
     * @see #isAdmin
     * @see #isAdmin() 
     */
    public void setAdmin(boolean isAdmin) throws SecurityException {
        LoginService.checkAccess(this);
        readWriteLock.writeLock().lock();
        try {
            this.isAdmin = isAdmin;
            UserStore.notifyUserChange(this);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
    
    /**
     * Checks whether the current user as a key with the given alias
     * @param alias the alias to check
     * @return whether the current user as a key with the given alias
     * @throws SecurityException if the currently logged in user does not have the permissions to view this user's keys
     * @see #getKey(java.lang.String) 
     * @see #setKey(java.lang.String, java.security.Key) 
     * @see #removeKey(java.lang.String) 
     * @see HashMap#containsKey(java.lang.Object) 
     */
    public boolean hasKey(String alias) throws SecurityException {
        LoginService.checkAccess(this);
        readWriteLock.readLock().lock();
        try {
            return keyStore.containsKey(alias);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    /**
     * Retrieves a key from the this user with the specified alias or <code>null</code> if none could be found
     * @param alias the alias to retrieve
     * @return a key from the this user with the specified alias
     * @throws SecurityException if the currently logged in user does not have the permissions to view this user's keys
     * @see #hasKey(java.lang.String) 
     * @see #setKey(java.lang.String, java.security.Key) 
     * @see #removeKey(java.lang.String) 
     * @see HashMap#get(java.lang.Object) 
     */
    public Key getKey(String alias) throws SecurityException {
        LoginService.checkAccess(this);
        readWriteLock.readLock().lock();
        try {
            return keyStore.get(alias);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    /**
     * Associates the given key to the given alias in this user's keystore
     * @param alias the alias to associate
     * @param key the key to associate
     * @throws SecurityException if the currently logged in user does not have the permissions to edit this user's keys
     * @see #hasKey(java.lang.String) 
     * @see #getKey(java.lang.String) 
     * @see #removeKey(java.lang.String) 
     * @see HashMap#put(java.lang.Object, java.lang.Object) 
     */
    public void setKey(String alias, Key key) throws SecurityException {
        LoginService.checkAccess(this);
        readWriteLock.readLock().lock();
        try {
            keyStore.put(alias, key);
            UserStore.notifyUserChange(this);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    /**
     * Removes a key from the this user with the specified alias
     * @param alias the alias to remove
     * @throws SecurityException if the currently logged in user does not have the permissions to edit this user's keys
     * @see #hasKey(java.lang.String) 
     * @see #getKey(java.lang.String) 
     * @see #setKey(java.lang.String, java.security.Key) 
     * @see HashMap#remove(java.lang.Object) 
     */
    public void removeKey(String alias) throws SecurityException {
        LoginService.checkAccess(this);
        readWriteLock.readLock().lock();
        try {
            keyStore.remove(alias);
            UserStore.notifyUserChange(this);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
}