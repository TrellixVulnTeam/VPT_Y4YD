package common.networking.packet.packets.result;

/**
 * An enum representing the type of a {@link ResultType}
 */
public enum ResultType {
    
    /**
     * Represents a null response (this should be ignored by the client)
     */
    NULL(-1),
    /**
     * Represents a server error
     */
    SERVER_ERROR(0),
    /**
     * Represents a response to a request preformed without the permissions to do so
     */
    ILLEGAL_ACCESS(1),
    /**
     * Represents an invalid request from the client
     */
    INVALID_REQUEST(2),
    /**
     * Represents a response to too many requests from the client
     */
    TOO_MANY_REQUESTS(3),
    /**
     * Represents a response to a request which consists of a success flag and a possible error message
     */
    STANDARD_RESULT(4);
    
    /**
     * A unique id associated with this ResultType
     */
    public final int id;

    /**
     * Creates a new ResultType
     * @param id the unique id to associate with this ResultType
     */
    private ResultType(int id) {
        this.id = id;
    }
    
}