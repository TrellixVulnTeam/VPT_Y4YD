package server.user;

import common.user.AttributeSearchCriteria;
import common.user.NetUserAttribute;
import common.user.UserAttributeType;
import java.io.Serializable;

/**
 * Represents an attribute associated with a user
 */
public abstract class UserAttribute implements Serializable {
    
    private static final long serialVersionUID = -8240452626850634012L;
    
    /**
     * The type of this attribute
     */
    public final UserAttributeType type;

    /**
     * Creates a new UserAttribute
     * @param type the type of this attribute
     */
    public UserAttribute(UserAttributeType type) {
        this.type = type;
    }
    
    /*
    At the time of writing documentation, I cannot recall the usage of this method nor find any references to it.
    As such, it has been removed for the time being. If it is needed later, it will be restored
    */
//    public abstract void authorize(User appliedUser);
    
    /**
     * Converts this UserAttribute to a NetUserAttribute
     * @return a NetUserAttribute containing the same information as this UserAttribute
     */
    public abstract NetUserAttribute toNetUserAttribute();
    
    /**
     * Checks the number of matches to the specified search in this UserAttribute
     * @param search the criteria to search
     * @return the number of matches to the specified search in this UserAttribute
     * @see UserStore#search(common.user.AttributeSearch) 
     */
    public abstract int search(AttributeSearchCriteria search);
    
}