package common;

/**
 * Represents a segment of data about a user
 */
public enum UserDataField {
    
    /**
     * <code>null</code>. This will be interpreted to mean no data
     */
    NULL(-1),
    /**
     * Represents a user's username
     */
    USERNAME(0),
    /**
     * Represents a user's usericon
     */
    USERICON(1);
    
    /**
     * A unique id associated with this UserDataField
     */
    public final int id;
    
    private UserDataField(int id) {
        this.id = id;
    }
    
}