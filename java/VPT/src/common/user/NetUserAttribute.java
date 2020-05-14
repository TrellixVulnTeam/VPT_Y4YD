package common.user;

import java.io.Serializable;

public class NetUserAttribute implements Serializable {
    
    private static final long serialVersionUID = -8616629287347204130L;
    
    public final UserAttributeType type;

    public NetUserAttribute(UserAttributeType type) {
        this.type = type;
    }
}
