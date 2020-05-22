package common.networking.packet.packets.result;

/**
 * A class representing a {@link ResultPacket} corresponding to a general error
 */
public class ErrorResultPacket extends ResultPacket {
    
    private static final long serialVersionUID = 1958928940460357927L;

    /**
     * Represents an ErrorResultPacket corresponding to a server error
     */
    public static final ErrorResultPacket SERVER_ERROR = new ErrorResultPacket(ResultType.SERVER_ERROR, "Server Error");
    
    /**
     * Represents an ErrorResultPacket corresponding to a server errorn invalid request from the client
     */
    public static final ErrorResultPacket INVALID_REQUEST = new ErrorResultPacket(ResultType.INVALID_REQUEST, "Invalid Request");
    
    /**
     * Creates an ErrorResultPacket corresponding to a request from the client made without the permissions to do so
     * @param reason the reason the client cannot preform the request
     * @return an ErrorResultPacket corresponding to a request from the client made without the permissions to do so
     */
    public static final ErrorResultPacket ILLEGAL_ACCESS(String reason) {
        return new ErrorResultPacket(ResultType.ILLEGAL_ACCESS, reason == null ? "" : reason);
    }
    
    /**
     * Creates an ErrorResultPacket corresponding to a too many requests from the client
     * @param timeout the time until the request can be tried again
     * @return an ErrorResultPacket corresponding to a too many requests from the client
     */
    public static final ErrorResultPacket TOO_MANY_REQUESTS(String timeout) {
        return new ErrorResultPacket(ResultType.TOO_MANY_REQUESTS, "Too Many Requests. Try again" + (timeout == null ? "" : " in " + timeout));
    }
    
    /**
     * Creates a new ErrorResultPacket
     * @param resultType the {@link ResultType} of the packet
     * @param msg an informational message about the error. The behavior if this is <code>null</code> is undefined
     * @see ResultPacket#ResultPacket(common.networking.packet.packets.result.ResultType, boolean, java.lang.String) 
     */
    public ErrorResultPacket(ResultType resultType, String msg) {
        super(resultType, false, msg);
    }
    
    /**
     * Creates a new ErrorResultPacket
     * @param resultType the result type of the packet
     * @param msg an informational message about the error. The behavior if this is <code>null</code> is undefined
     * @see ResultPacket#ResultPacket(int, boolean, java.lang.String) 
     */
    public ErrorResultPacket(int resultType, String msg) {
        super(resultType, false, msg);
    }

}
