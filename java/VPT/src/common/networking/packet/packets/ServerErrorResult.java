package common.networking.packet.packets;

public class ServerErrorResult extends ResultPacket {

    public ServerErrorResult() {
        super(ResultType.SERVER_ERROR);
    }
    
}