package server.user;

import common.user.NetPublicUser;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

public class PublicUser implements Serializable {
    
    protected ReadWriteLock readWriteLock;
    private static final long serialVersionUID = 647217896180882120L;

    public final String userID;
    private final ArrayList<UserAttribute> attributes;

    public PublicUser(String userID) {
        this(userID, new ArrayList<>());
    }

    public PublicUser(String userID, Collection<UserAttribute> attributes) {
        this.userID = userID;
        this.attributes = new ArrayList<>(attributes);
    }
    
    public final ArrayList<UserAttribute> getAttributes() {
        readWriteLock.readLock().lock();
        try {
            return new ArrayList<>(attributes);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    public final NetPublicUser toNetPublicUser() {
        readWriteLock.readLock().lock();
        try {
            return new NetPublicUser(userID, attributes.stream().map((attribute) -> attribute.toNetUserAttribute()).collect(Collectors.toList()));
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
}