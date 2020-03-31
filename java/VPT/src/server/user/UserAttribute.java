package server.user;

import common.user.NetUserAttribute;

public abstract class UserAttribute {
    
    public abstract void authorize(User appliedUser);
    
    public abstract NetUserAttribute toNetUserAttribute();
    
}