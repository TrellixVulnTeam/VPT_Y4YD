package common.networking.packet.packets.result;

/**
 * Represents a {@link ResultPacket} with two pieces of data attached
 * @param <T> the type of the first attached data
 * @param <U> the type of the second attached data
 */
public class DoubleResultPacket<T, U> extends ResultPacket {
    
    private static final long serialVersionUID = 7923660726769306567L;
    
    /**
     * The first data included with this DoubleResultPacket
     */
    public final T result1;
    /**
     * The second data included with this DoubleResultPacket
     */
    public final U result2;

    /**
     * Creates a new DoubleResultPacket
     * @param resultType The {@link ResultType} of this DoubleResultPacket
     * @param wasActionSuccessful Was the client's request was successful?
     * @param msg an informational message about the error. The behavior if this is <code>null</code>
     * is undefined if <code>wasActionSuccessful</code> is <code>false</code>
     * @param result1 the first data to include with this SingleResultPacket
     * @param result2 the second data to include with this SingleResultPacket
     */
    public DoubleResultPacket(ResultType resultType, boolean wasActionSuccessful, String msg, T result1, U result2) {
        this(resultType.id, wasActionSuccessful, msg, result1, result2);
    }

    /**
     * Creates a new DoubleResultPacket
     * @param resultType The result type of this DoubleResultPacket
     * @param wasActionSuccessful Was the client's request was successful?
     * @param msg an informational message about the error. The behavior if this is <code>null</code>
     * is undefined if <code>wasActionSuccessful</code> is <code>false</code>
     * @param result1 the first data to include with this SingleResultPacket
     * @param result2 the second data to include with this SingleResultPacket
     */
    public DoubleResultPacket(int resultType, boolean wasActionSuccessful, String msg, T result1, U result2) {
        super(resultType, wasActionSuccessful, msg);
        this.result1 = result1;
        this.result2 = result2;
    }
    
}