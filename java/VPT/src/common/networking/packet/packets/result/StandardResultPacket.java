package common.networking.packet.packets.result;

/**
 * A class providing methods which create standard {@link ResultPacket}s
 */
public class StandardResultPacket extends ResultPacket {
    
    /**
     * Creates a new StandardResultPacket from the given information. This is equivalent to <code>new StandardResultPacket(result, null)</code>
     * @param result was the request successful?
     */
    public StandardResultPacket(boolean result) {
        this(result, null);
    }
    
    /**
     * Creates a new StandardResultPacket from the given information. If <code>msg</code> is <code>null</code> and
     * <code>result</code> is <code>false</code>, <code>msg</code> will be made <code>""</code>
     * @param result was the request successful?
     * @param msg a message included with the result
     */
    public StandardResultPacket(boolean result, String msg) {
        super(ResultType.STANDARD_RESULT, result, result ? msg : msg == null ? "" : msg);
    }
    
}