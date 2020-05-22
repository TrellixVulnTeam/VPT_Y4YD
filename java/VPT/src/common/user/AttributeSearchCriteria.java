package common.user;

/**
 * Represents search criteria for an {@link AttributeSearch}
 */
public class AttributeSearchCriteria {

    /**
     * The location this criteria applies to
     */
    public final AttributeSearchLocation location;
    /**
     * The string to search for
     */
    public final String search;
    /**
     * Whether this criteria is a blacklist
     */
    public final boolean isBlacklist;
    /**
     * Should the search string be quoted
     */
    public final boolean quote;
    /**
     * Should the search string match case
     */
    public final boolean matchCase;

    /**
     * Creates a new AttributeSearchCriteria
     * @param location the location this criteria applies to
     * @param search the string to search for
     * @param isBlacklist whether this criteria is a blacklist
     * @param quote should the search string be quoted
     * @param matchCase should the search string match case
     */
    public AttributeSearchCriteria(AttributeSearchLocation location, String search, boolean isBlacklist, boolean quote, boolean matchCase) {
        this.location = location;
        this.search = search;
        this.isBlacklist = isBlacklist;
        this.quote = quote;
        this.matchCase = matchCase;
    }
    
}