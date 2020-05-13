package common.user;

public enum AttributeSearchLocation {
    
    ANYWHERE, USERID;
    
    public final boolean isIrregular;
    public final UserAttributeType equivilentType;
    
    private AttributeSearchLocation() {
        this(null);
    }
    
    private AttributeSearchLocation(UserAttributeType equivilentType) {
        this(equivilentType == null, equivilentType);
    }
    
    private AttributeSearchLocation(boolean isIrregular, UserAttributeType equivilentType) {
        this.isIrregular = isIrregular;
        this.equivilentType = equivilentType;
    }
    
}