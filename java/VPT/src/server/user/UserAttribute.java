package server.user;

import common.user.AttributeSearchCriteria;
import common.user.NetUserAttribute;
import common.user.UserAttributeType;
import java.io.Serializable;

public abstract class UserAttribute implements Serializable {
    
    private static final long serialVersionUID = -8240452626850634012L;
    
    public final UserAttributeType type;

    public UserAttribute(UserAttributeType type) {
        this.type = type;
    }
    
    public abstract void authorize(User appliedUser);
    
    public abstract NetUserAttribute toNetUserAttribute();
    
    public abstract int search(AttributeSearchCriteria search);
    
}