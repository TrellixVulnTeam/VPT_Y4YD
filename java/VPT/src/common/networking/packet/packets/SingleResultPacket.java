package common.networking.packet.packets;

public class SingleResultPacket<T> extends ResultPacket {
    
    public final T result;

    public SingleResultPacket(T result, ResultType resultType) {
        this(result, resultType.id);
    }

    public SingleResultPacket(T result, int resultType) {
        super(resultType);
        this.result = result;
    }
    
}