package common.networking.packet.packets;

public enum ResultType {
    
    NULL(-1), SERVER_ERROR(0), ILLEGAL_ACCESS(1), LOGIN(2);
    
    public final int id;

    private ResultType(int id) {
        this.id = id;
    }
    
}