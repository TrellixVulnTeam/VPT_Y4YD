package common.networking.packet.packets.result;

/**
 * Represents a {@link ResultPacket} with a piece of data attached
 * @param <T> the type of the attached data
 */
public class SingleResultPacket<T> extends ResultPacket {
    
    private static final long serialVersionUID = 7923660726769306567L;
    
    /**
     * The data included with this SingleResultPacket
     */
    public final T result;

    /**
     * Creates a new SingleResultPacket
     * @param resultType The {@link ResultType} of this SingleResultPacket
     * @param wasActionSuccessful Was the client's request was successful?
     * @param msg an informational message about the error. The behavior if this is <code>null</code>
     * is undefined if <code>wasActionSuccessful</code> is <code>false</code>
     * @param result the data to include with this SingleResultPacket
     */
    public SingleResultPacket(ResultType resultType, boolean wasActionSuccessful, String msg, T result) {
        this(resultType.id, wasActionSuccessful, msg, result);
    }

    /**
     * Creates a new SingleResultPacket
     * @param resultType The result type of this SingleResultPacket
     * @param wasActionSuccessful Was the client's request was successful?
     * @param msg an informational message about the error. The behavior if this is <code>null</code>
     * is undefined if <code>wasActionSuccessful</code> is <code>false</code>
     * @param result the data to include with this SingleResultPacket
     */
    public SingleResultPacket(int resultType, boolean wasActionSuccessful, String msg, T result) {
        super(resultType, wasActionSuccessful, msg);
        this.result = result;
    }
    
}