package server.user;

import common.user.NetUserAttribute;
import common.user.UserAttributeType;

public abstract class UserAttribute {
    
    public final UserAttributeType type;

    public UserAttribute(UserAttributeType type) {
        this.type = type;
    }
    
    public abstract void authorize(User appliedUser);
    
    public abstract NetUserAttribute toNetUserAttribute();
    
    public abstract int search(String search);
    
}