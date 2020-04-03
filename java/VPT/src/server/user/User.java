package server.user;

import common.Utils;
import java.util.ArrayList;
import java.util.Arrays;

public class User extends PublicUser {

    private byte[] password;
    private boolean isVisible, isAdmin;
    
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
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
    
}