package common.networking.packet;

public enum PacketId {
    
    NULL(-1), SERVER_STATUS(0), FORCE_LOGOUT(1), LOGIN(2), RESULT(3), CREATE_USER(4), DELETE_USER(5);
    
    public final int id;

    private PacketId(int id) {
        this.id = id;
    }
    
}