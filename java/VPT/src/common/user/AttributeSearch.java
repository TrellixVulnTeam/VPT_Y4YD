package common.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Represents a set of search criteria
 * @see server.user.UserStore#search(common.user.AttributeSearch) 
 */
public class AttributeSearch {
    
    /**
     * The search criteria
     */
    private final ArrayList<AttributeSearchCriteria> criteria;
    
    /**
     * Creates a new AttributeSearch with the specified criteria
     * @param criteria the search criteria
     */
    public AttributeSearch(AttributeSearchCriteria... criteria) {
        this(Arrays.asList(criteria));
    }
    
    /**
     * Creates a new AttributeSearch with the specified criteria
     * @param criteria the search criteria
     */
    public AttributeSearch(Collection<AttributeSearchCriteria> criteria) {
        this.criteria = new ArrayList<>(criteria);
    }
    
    /**
     * Retrieves the search criteria
     * @return the search criteria
     */
    public ArrayList<AttributeSearchCriteria> getCriteria() {
        return new ArrayList<>(criteria);
    }
    
}