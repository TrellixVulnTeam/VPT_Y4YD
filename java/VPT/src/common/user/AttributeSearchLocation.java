package common.user;

/**
 * Represents the location an {@link AttributeSearchCriteria} searches
 */
public enum AttributeSearchLocation {
    
    /**
     * Represents a search which searches anywhere
     */
    ANYWHERE,
    /**
     * Represents a search which searches userIds
     */
    USERID;
    
    /**
     * Whether this location is irregular (doesn't have an {@link #equivilentType}
     */
    public final boolean isIrregular;
    /**
     * The equivalent {@link UserAttributeType} or <code>null</code> if none exists
     */
    public final UserAttributeType equivilentType;
    
    /**
     * Creates a new irregular AttributeSearchLocation
     * @see #isIrregular
     */
    private AttributeSearchLocation() {
        this(null);
    }
    
    /**
     * Creates a new optionally-irregular AttributeSearchLocation with the given {@link #equivilentType} or <code>null</code> if none exists
     * @param equivilentType the {@link #equivilentType} of this AttributeSearchLocation or <code>null</code> if none exists
     * @see #isIrregular
     */
    private AttributeSearchLocation(UserAttributeType equivilentType) {
        this(equivilentType == null, equivilentType);
    }
    
    /**
     * Creates a new optionally-irregular AttributeSearchLocation with the given {@link #equivilentType} or <code>null</code> if none exists
     * @param isIrregular whether this AttributeSearchLocation is irregular
     * @param equivilentType the {@link #equivilentType} of this AttributeSearchLocation or <code>null</code> if none exists
     * @see #isIrregular
     */
    private AttributeSearchLocation(boolean isIrregular, UserAttributeType equivilentType) {
        this.isIrregular = isIrregular;
        this.equivilentType = equivilentType;
    }
    
}