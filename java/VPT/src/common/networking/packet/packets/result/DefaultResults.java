package common.networking.packet.packets.result;

public final class DefaultResults {
    
    public static ResultPacket login(boolean result) {
        return createResult(ResultType.LOGIN, result, result ? null : "Invalid Login");
    }
    
    public static ResultPacket createUser(boolean result) {
        return createUser(result, null);
    }
    
    public static ResultPacket createUser(boolean result, String msg) {
        return createResult(ResultType.CREATE_USER, result, msg);
    }
    
    public static ResultPacket createResult(ResultType resultType, boolean result, String msg) {
        return createResult(resultType.id, result, msg);
    }
    
    public static ResultPacket createResult(int resultType, boolean result, String msg) {
        return new ResultPacket(resultType, result, result ? msg : msg == null ? "" : msg);
    }
    
    private DefaultResults() {}
    
}