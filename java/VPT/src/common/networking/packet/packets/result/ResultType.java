package common.networking.packet.packets.result;

public enum ResultType {
    
    NULL(-1), SERVER_ERROR(0), ILLEGAL_ACCESS(1), INVALID_REQUEST(2), TOO_MANY_REQUESTS(3), LOGIN(4), CREATE_USER(5), DELETE_USER(6);
    
    public final int id;

    private ResultType(int id) {
        this.id = id;
    }
    
}