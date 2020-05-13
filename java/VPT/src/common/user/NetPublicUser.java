package common.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class NetPublicUser implements Serializable {
    
    private static final long serialVersionUID = 3943087653729020969L;

    public final String userId;
    private final Collection<NetUserAttribute> attributes;

    public NetPublicUser(String userId) {
        this(userId, new ArrayList<>());
    }

    public NetPublicUser(String userId, Collection<NetUserAttribute> attributes) {
        this.userId = userId;
        this.attributes = attributes;
    }
    
    public final ArrayList<NetUserAttribute> getAttributes() {
        return new ArrayList<>(attributes);
    }
    
}