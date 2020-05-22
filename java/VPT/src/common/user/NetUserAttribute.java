package common.user;

import java.io.Serializable;

/**
 * Represents an attribute associated with a user
 */
public class NetUserAttribute implements Serializable {
    
    private static final long serialVersionUID = -8616629287347204130L;
    
    /**
     * The type of this attribute
     */
    public final UserAttributeType type;

    /**
     * Creates a new UserAttribute
     * @param type the type of this attribute
     */
    public NetUserAttribute(UserAttributeType type) {
        this.type = type;
    }
}
