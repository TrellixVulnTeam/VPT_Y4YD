package common.networking.packet.packets.result;

public class DoubleResultPacket<T, U> extends ResultPacket {
    
    private static final long serialVersionUID = 7923660726769306567L;
    
    public final T result1;
    public final U result2;

    public DoubleResultPacket(ResultType resultType, boolean wasActionSuccessful, String msg, T result1, U result2) {
        this(resultType.id, wasActionSuccessful, msg, result1, result2);
    }

    public DoubleResultPacket(int resultType, boolean wasActionSuccessful, String msg, T result1, U result2) {
        super(resultType, wasActionSuccessful, msg);
        this.result1 = result1;
        this.result2 = result2;
    }
    
}