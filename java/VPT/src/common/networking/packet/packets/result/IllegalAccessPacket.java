package common.networking.packet.packets.result;

public class IllegalAccessPacket extends ResultPacket {

    public IllegalAccessPacket(String reason) {
        super(ResultType.ILLEGAL_ACCESS, false, reason == null ? "" : reason);
    }
    
}