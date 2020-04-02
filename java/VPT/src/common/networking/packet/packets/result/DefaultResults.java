package common.networking.packet.packets.result;

public final class DefaultResults {
    
    public static ResultPacket login(boolean result) {
        return new ResultPacket(ResultType.LOGIN, result, result ? null : "Invalid Login");
    }
    
    private DefaultResults() {}
    
}