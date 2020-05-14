package server.user;

import common.Utils;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class User extends PublicUser {

    private static final long serialVersionUID = -732320744263029685L;
    
    private byte[] password;
    private boolean isVisible, isAdmin;
    private final HashMap<String, Key> keyStore;
    
    public User(String userId, byte[] password) {
        this(userId, password, false);
    }

    public User(String userID, byte[] password, boolean isAdmin) {
        this(userID, password, isAdmin, false);
    }

    public User(String userID, byte[] password, boolean isAdmin, boolean isVisible) {
        this(userID, password, isAdmin, isVisible, new ArrayList<>());
    }

    public User(String userID, byte[] password, boolean isAdmin, boolean isVisible, ArrayList<UserAttribute> attributes) {
        super(userID, attributes);
        this.password = Utils.createMD().digest(password);
        this.isVisible = isVisible;
        this.isAdmin = isAdmin;
        this.keyStore = new HashMap<>();
        keyStore.put("USER_FILE_PUBLIC_KEY", Utils.createPasswordKey(password));
    }
    
    public boolean checkPassword(byte[] password) {
        return Arrays.equals(this.password, Utils.createMD().digest(password));
    }

    public boolean isVisible() {
        readWriteLock.readLock().lock();
        try {
            return isVisible;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

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

    public boolean isAdmin() {
        readWriteLock.readLock().lock();
        try {
            return isAdmin;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

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
    
    public boolean hasKey(String name) throws SecurityException {
        LoginService.checkAccess(this);
        readWriteLock.readLock().lock();
        try {
            return keyStore.containsKey(name);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    public Key getKey(String name) throws SecurityException {
        LoginService.checkAccess(this);
        readWriteLock.readLock().lock();
        try {
            return keyStore.get(name);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    public void setKey(String name, Key key) throws SecurityException {
        LoginService.checkAccess(this);
        readWriteLock.readLock().lock();
        try {
            keyStore.put(name, key);
            UserStore.notifyUserChange(this);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    public void removeKey(String name) throws SecurityException {
        LoginService.checkAccess(this);
        readWriteLock.readLock().lock();
        try {
            keyStore.remove(name);
            UserStore.notifyUserChange(this);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
}