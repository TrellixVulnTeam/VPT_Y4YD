package common.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents the publicly available attributes of a user
 */
public class NetPublicUser implements Serializable {
    
    private static final long serialVersionUID = 3943087653729020969L;

    /**
     * The userId of this User
     */
    public final String userId;
    /**
     * The attributes associated with this user
     */
    private final Collection<NetUserAttribute> attributes;

    /**
     * Creates a new NetPublicUser with the given userId
     * @param userId the userId to associate with this NetPublicUser
     */
    public NetPublicUser(String userId) {
        this(userId, new ArrayList<>());
    }

    /**
     * Creates a new NetPublicUser with the given userId and attributes
     * @param userId the userId to associate with this NetPublicUser
     * @param attributes the attributes to associate with this NetPublicUser
     */
    public NetPublicUser(String userId, Collection<NetUserAttribute> attributes) {
        this.userId = userId;
        this.attributes = attributes;
    }
    
    /**
     * Retrieves the attributes associated with this user
     * @return the attributes associated with this user
     */
    public final ArrayList<NetUserAttribute> getAttributes() {
        return new ArrayList<>(attributes);
    }
    
}