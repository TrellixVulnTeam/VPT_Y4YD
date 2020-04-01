package common.networking.packet.packets.result;

public class SingleResultPacket<T> extends ResultPacket {
    
    public final T result;

    public SingleResultPacket(ResultType resultType, boolean wasActionSuccessful, String msg, T result) {
        this(resultType.id, wasActionSuccessful, msg, result);
    }

    public SingleResultPacket(int resultType, boolean wasActionSuccessful, String msg, T result) {
        super(resultType, wasActionSuccessful, msg);
        this.result = result;
    }
    
}