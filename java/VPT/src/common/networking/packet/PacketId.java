package common.networking.packet;

/**
 * An enum representing the data type carried by a {@link Packet}
 */
public enum PacketId {
    
    /**
     * Represents a Packet with no data (this should be ignored
     */
    NULL(-1),
    /**
     * Represents a Packet carrying the status of the server
     */
    SERVER_STATUS(0),
    /**
     * Represents a Packet forcing the logout of a client
     */
    FORCE_LOGOUT(1),
    /**
     * Represents a Packet requesting that the client be logged in
     */
    LOGIN(2),
    /**
     * Represents a {@link common.networking.packet.packets.result.ResultPacket}
     */
    RESULT(3),
    /**
     * Represents a Packet requesting that the server shutdown
     */
    SHUTDOWN(4),
    /**
     * Represents a Packet requesting the creation of a user
     */
    CREATE_USER(5),
    /**
     * Represents a Packet requesting the deletion of a user
     */
    DELETE_USER(6);
    
    /**
     * A unique id associated with this PacketId
     */
    public final int id;

    /**
     * Creates a new PacketId
     * @param id the unique id to associate with this PacketId
     */
    private PacketId(int id) {
        this.id = id;
    }
    
}