package server.user;

import common.user.NetPublicUser;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import server.ServerConstants;

/**
 * Contains the publicly accessible attributes of a {@link User}
 */
public class PublicUser implements Serializable {
    
    /**
     * A lock protecting access to the properties of this User
     */
    protected ReadWriteLock readWriteLock = new ReentrantReadWriteLock(ServerConstants.USE_FAIR_LOCKS);
    
    private static final long serialVersionUID = 647217896180882120L;

    /**
     * The userId of this User
     */
    public final String userId;
    /**
     * The attributes associated with this User
     */
    private final ArrayList<UserAttribute> attributes;

    /**
     * Creates a new PublicUser with the given userId
     * @param userId the userId to associate with this PublicUser
     */
    public PublicUser(String userId) {
        this(userId, new ArrayList<>());
    }

    /**
     * Creates a new PublicUser with the given userId and attributes
     * @param userId the userId to associate with this PublicUser
     * @param attributes the attributes to associate with this PublicUser
     */
    public PublicUser(String userId, Collection<UserAttribute> attributes) {
        this.userId = userId;
        this.attributes = new ArrayList<>(attributes);
    }
    
    /**
     * Retrieves the attributes associated with this User
     * @return the attributes associated with this User
     */
    public final ArrayList<UserAttribute> getAttributes() {
        readWriteLock.readLock().lock();
        try {
            return new ArrayList<>(attributes);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    /**
     * Converts this PublicUser to a NetPublicUser
     * @return a NetPublicUser containing the same information as this PublicUser
     */
    public final NetPublicUser toNetPublicUser() {
        readWriteLock.readLock().lock();
        try {
            return new NetPublicUser(userId, attributes.stream().map((attribute) -> attribute.toNetUserAttribute()).collect(Collectors.toList()));
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    public void addAttribute(User thisUser, UserAttribute attr) throws SecurityException {
        if(thisUser != this) {
            throw new SecurityException("Invalid User Reference");
        }
        LoginService.checkAccess(thisUser);
        readWriteLock.writeLock().lock();
        try {
            attributes.add(attr);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
    
}