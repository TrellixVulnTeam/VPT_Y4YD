package common.networking.packet.packets;

public enum ResultType {
    
    NULL(-1), LOGIN(1);
    
    public final int id;

    private ResultType(int id) {
        this.id = id;
    }
    
}