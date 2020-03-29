package common.user;

import java.io.Serializable;

public class PartialUser implements Serializable {
    
    private static final long serialVersionUID = 3943087653729020969L;

    public final String userID;
    public final String username;

    public PartialUser(String userID, String username) {
        this.userID = userID;
        this.username = username;
    }
    
}