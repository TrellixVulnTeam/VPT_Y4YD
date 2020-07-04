package server.user;

import common.Utils;
import common.user.AttributeSearchCriteria;
import common.user.NetSingleDataAttribute;
import common.user.NetUserAttribute;
import common.user.UserAttributeType;
import java.io.Serializable;

public abstract class SingleDataAttribute<T extends Serializable> extends UserAttribute {
    
    private static final long serialVersionUID = 1373918710933044666L;

    private T data;
    private final User user;
    
    public SingleDataAttribute(UserAttributeType type, T data, User user) {
        super(type);
        this.data = data;
        this.user = user;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        LoginService.checkAccess(user);
        this.data = data;
    }
    
    @Override
    public NetUserAttribute toNetUserAttribute() {
        return new NetSingleDataAttribute<>(type, data);
    }
    
    public static class NoSearch<T extends Serializable> extends SingleDataAttribute<T> {
        
        private static final long serialVersionUID = -1359133676680050924L;
    
        public NoSearch(UserAttributeType type, T data, User user) {
            super(type, data, user);
        }
        
        @Override
        public int search(AttributeSearchCriteria search) {
            return 0;
        }
        
    }
    
    public static class StringSearch<T extends Serializable> extends SingleDataAttribute<T> {

        private static final long serialVersionUID = 5551815009284053455L;
    
        public StringSearch(UserAttributeType type, T data, User user) {
            super(type, data, user);
        }
        
        @Override
        public int search(AttributeSearchCriteria search) {
            return Math.max(1, Utils.countStringMatches(getData().toString(), search.search, search.quote, search.matchCase));
        }
        
    }
    
}