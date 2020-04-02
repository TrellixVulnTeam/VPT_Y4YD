package common.networking.packet.packets.result;

public class ErrorResultPacket extends ResultPacket {

    public static final ErrorResultPacket SERVER_ERROR = new ErrorResultPacket(ResultType.SERVER_ERROR, "Server Error");
    public static final ErrorResultPacket INVALID_REQUEST = new ErrorResultPacket(ResultType.INVALID_REQUEST, "Invalid Request");
    
    public static final ErrorResultPacket ILLEGAL_ACCESS(String reason) {
        return new ErrorResultPacket(ResultType.ILLEGAL_ACCESS, reason == null ? "" : reason);
    }
    
    public ErrorResultPacket(ResultType resultType, String msg) {
        super(resultType, false, msg);
    }

    public ErrorResultPacket(int resultType, String msg) {
        super(resultType, false, msg);
    }

}
