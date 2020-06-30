package common.networking.packet.packets.result;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

/**
 * A packet sent in response to a client request
 */
public class ResultPacket extends Packet {

    private static final long serialVersionUID = 2298268915644736483L;
    
    /**
     * An integer representing the type of result packet
     * @see ResultType
     */
    public final int resultType;
    /**
     * A boolean representing whether the client's request was successful
     */
    public final boolean wasActionSuccessful;
    /**
     * A String containing an informational message about the request. If <code>wasActionSuccessful</code> is true, this may be <code>null</code>
     */
    public final String msg;

    /**
     * Creates a new ResultPacket
     * @param resultType the {@link ResultType} of the packet
     * @param wasActionSuccessful Was the client's request was successful?
     * @param msg an informational message about the error. The behavior if this is <code>null</code>
     * is undefined if <code>wasActionSuccesssul</code> is <code>false</code>
     */
    public ResultPacket(ResultType resultType, boolean wasActionSuccessful, String msg) {
        this(resultType.id, wasActionSuccessful, msg);
    }
    
    /**
     * Creates a new ResultPacket
     * @param resultType the result type of the packet
     * @param wasActionSuccessful Was the client's request was successful?
     * @param msg an informational message about the error. The behavior if this is <code>null</code>
     * is undefined if <code>wasActionSuccesssul</code> is <code>false</code>
     */
    public ResultPacket(int resultType, boolean wasActionSuccessful, String msg) {
        super(PacketId.RESULT);
        this.resultType = resultType;
        this.wasActionSuccessful = wasActionSuccessful;
        this.msg = msg;
    }
    
}