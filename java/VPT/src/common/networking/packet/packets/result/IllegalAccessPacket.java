package common.networking.packet.packets.result;

import common.networking.packet.packets.result.ResultPacket;
import common.networking.packet.packets.result.ResultType;

public class IllegalAccessPacket extends ResultPacket {

    public IllegalAccessPacket() {
        super(ResultType.ILLEGAL_ACCESS);
    }
    
}