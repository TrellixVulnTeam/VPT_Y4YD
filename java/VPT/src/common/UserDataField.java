package common;

public enum UserDataField {
    
    NULL(-1),
    USERNAME(0),
    USERICON(1);
    
    public final int id;
    
    private UserDataField(int id) {
        this.id = id;
    }
    
}