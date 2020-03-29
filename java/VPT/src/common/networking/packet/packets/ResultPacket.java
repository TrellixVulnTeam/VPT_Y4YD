package common.networking.packet.packets;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

public class ResultPacket extends Packet {

    public final int resultType;

    public ResultPacket(ResultType resultType) {
        this(resultType.id);
    }
    
    public ResultPacket(int resultType) {
        super(PacketId.RESULT);
        this.resultType = resultType;
    }
    
}