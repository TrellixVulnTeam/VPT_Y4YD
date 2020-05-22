package common.networking.packet.packets.result;

/**
 * A class providing methods which create standard {@link ResultPacket}s
 */
public final class DefaultResults {
    
    /**
     * Creates a {@link ResultPacket} for a login request
     * @param result Was the request successful?
     * @return a {@link ResultPacket} for a login request
     */
    public static ResultPacket login(boolean result) {
        return createResult(ResultType.LOGIN, result, result ? null : "Invalid Login");
    }
    
    /**
     * Creates a {@link ResultPacket} for a user creation request
     * @param result Was the request successful?
     * @return a {@link ResultPacket} for a user creation request
     */
    public static ResultPacket createUser(boolean result) {
        return createUser(result, null);
    }
    
    /**
     * Creates a {@link ResultPacket} for a user creation request
     * @param result Was the request successful?
     * @param msg a message included with the result
     * @return a {@link ResultPacket} for a user creation request
     */
    public static ResultPacket createUser(boolean result, String msg) {
        return createResult(ResultType.CREATE_USER, result, msg);
    }
    
    /**
     * Creates a {@link ResultPacket} for a user deletion request
     * @param result Was the request successful?
     * @return a {@link ResultPacket} for a user deletion request
     */
    public static ResultPacket deleteUser(boolean result) {
        return deleteUser(result, null);
    }
    
    /**
     * Creates a {@link ResultPacket} for a user deletion request
     * @param result Was the request successful?
     * @param msg a message included with the result
     * @return a {@link ResultPacket} for a user deletion request
     */
    public static ResultPacket deleteUser(boolean result, String msg) {
        return createResult(ResultType.DELETE_USER, result, msg);
    }
    
    /**
     * Creates a {@link ResultPacket} from the given information. The usage of
     * this method as <code>createResult(resultType,result,msg)</code> is equivelent
     * to <code>createResult(resultType.id,result,msg)</code>
     * @param resultType the {@link ResultType} of the packet
     * @param result was the request successful?
     * @param msg a message included with the result
     * @return a {@link ResultPacket} from the given information
     * @see #createResult(int, boolean, java.lang.String) 
     * @see ResultPacket#ResultPacket(common.networking.packet.packets.result.ResultType, boolean, java.lang.String) 
     */
    public static ResultPacket createResult(ResultType resultType, boolean result, String msg) {
        return createResult(resultType.id, result, msg);
    }
    
    /**
     * Creates a {@link ResultPacket} from the given information. If <code>msg</code> is <code>null</code> and
     * <code>result</code> is <code>false</code>, <code>msg</code> will be made <code>""</code>
     * @param resultType the result type of the packet
     * @param result was the request successful?
     * @param msg a message included with the result
     * @return a {@link ResultPacket} from the given information
     * @see #createResult(common.networking.packet.packets.result.ResultType, boolean, java.lang.String) 
     * @see ResultPacket#ResultPacket(int, boolean, java.lang.String) 
     */
    public static ResultPacket createResult(int resultType, boolean result, String msg) {
        return new ResultPacket(resultType, result, result ? msg : msg == null ? "" : msg);
    }
    
    private DefaultResults() {}
    
}