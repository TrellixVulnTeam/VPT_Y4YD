package common.networking.packet;

public enum PacketId {
    
    NULL(-1), FORCE_LOGOUT(0), LOGIN(1), RESULT(2);
    
    public final int id;

    private PacketId(int id) {
        this.id = id;
    }
    
}