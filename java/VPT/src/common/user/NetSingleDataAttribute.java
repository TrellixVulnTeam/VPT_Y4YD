package common.user;

public class NetSingleDataAttribute<T> extends NetUserAttribute {

    private static final long serialVersionUID = -4569709292196234875L;

    private T data;
    
    public NetSingleDataAttribute(UserAttributeType type, T data) {
        super(type);
    }

    public T getData() {
        return data;
    }
    
    
}