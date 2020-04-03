package common.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class AttributeSearch {
    
    private final ArrayList<AttributeSearchCriteria> criteria;
    
    public AttributeSearch(AttributeSearchCriteria... criteria) {
        this(Arrays.asList(criteria));
    }
    
    public AttributeSearch(Collection<AttributeSearchCriteria> criteria) {
        this.criteria = new ArrayList<>(criteria);
    }
    
    public ArrayList<AttributeSearchCriteria> getCriteria() {
        return new ArrayList<>(criteria);
    }
    
}