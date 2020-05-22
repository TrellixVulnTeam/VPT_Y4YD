package common.networking.packet.packets;

/**
 * An enum representing the status of the server
 */
public enum ServerStatus {
    
    /**
     * The server is operating normally
     */
    OK,
    /**
     * The server is temporarily offline
     */
    OFFLINE,
    /**
     * The server is operating normally, but this connection is being refused
     * because of too many connections from this client
     */
    TOO_MANY_REQUESTS;
    
}