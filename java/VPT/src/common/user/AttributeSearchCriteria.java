package common.user;

public class AttributeSearchCriteria {

    public final AttributeSearchLocation location;
    public final String search;
    public final boolean isBlacklist;
    
    public AttributeSearchCriteria(String search) {
        this(search, false);
    }

    public AttributeSearchCriteria(AttributeSearchLocation location, String search) {
        this(location, search, false);
    }

    public AttributeSearchCriteria(String search, boolean isBlacklist) {
        this(AttributeSearchLocation.ANYWHERE, search, isBlacklist);
    }

    public AttributeSearchCriteria(AttributeSearchLocation location, String search, boolean isBlacklist) {
        this.location = location;
        this.search = search;
        this.isBlacklist = isBlacklist;
    }
    
}