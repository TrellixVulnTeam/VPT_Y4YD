package common.user;

public class AttributeSearchCriteria {

    public final AttributeSearchLocation location;
    public final String search;
    public final boolean isBlacklist;
    public final boolean quote;
    public final boolean matchCase;

    public AttributeSearchCriteria(AttributeSearchLocation location, String search, boolean isBlacklist, boolean quote, boolean matchCase) {
        this.location = location;
        this.search = search;
        this.isBlacklist = isBlacklist;
        this.quote = quote;
        this.matchCase = matchCase;
    }
    
}