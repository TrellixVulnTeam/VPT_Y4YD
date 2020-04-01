package common.networking.packet.packets.result;

public class ServerErrorResult extends ResultPacket {

    public ServerErrorResult() {
        super(ResultType.SERVER_ERROR, false, "Server Error");
    }
    
}