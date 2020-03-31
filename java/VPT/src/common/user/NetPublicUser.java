package common.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class NetPublicUser implements Serializable {
    
    private static final long serialVersionUID = 3943087653729020969L;

    public final String userID;
    private final Collection<NetUserAttribute> attributes;

    public NetPublicUser(String userID) {
        this(userID, new ArrayList<>());
    }

    public NetPublicUser(String userID, Collection<NetUserAttribute> attributes) {
        this.userID = userID;
        this.attributes = attributes;
    }
    
    public final ArrayList<NetUserAttribute> getAttributes() {
        return new ArrayList<>(attributes);
    }
    
}