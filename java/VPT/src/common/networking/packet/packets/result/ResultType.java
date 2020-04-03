package common.networking.packet.packets.result;

public enum ResultType {
    
    NULL(-1), SERVER_ERROR(0), ILLEGAL_ACCESS(1), INVALID_REQUEST(2), LOGIN(3), CREATE_USER(4), DELETE_USER(5);
    
    public final int id;

    private ResultType(int id) {
        this.id = id;
    }
    
}