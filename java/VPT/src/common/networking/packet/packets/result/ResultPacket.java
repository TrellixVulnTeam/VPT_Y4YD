package common.networking.packet.packets.result;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

public class ResultPacket extends Packet {

    private static final long serialVersonUID = 2298268915644736483L;
    
    public final int resultType;
    public final boolean wasActionSuccessful;
    public final String msg;

    public ResultPacket(ResultType resultType, boolean wasActionSuccessful, String msg) {
        this(resultType.id, wasActionSuccessful, msg);
    }

    public ResultPacket(int resultType, boolean wasActionSuccessful, String msg) {
        super(PacketId.RESULT);
        this.resultType = resultType;
        this.wasActionSuccessful = wasActionSuccessful;
        this.msg = msg;
    }
    
}