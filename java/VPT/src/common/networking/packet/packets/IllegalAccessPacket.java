package common.networking.packet.packets;

public class IllegalAccessPacket extends ResultPacket {

    public IllegalAccessPacket() {
        super(ResultType.ILLEGAL_ACCESS);
    }
    
}