package common.networking.packet.packets;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

/**
 * A {@link Packet} requesting that the client be logged in to the specified user account
 */
public class LoginPacket extends Packet {
    
    private static final long serialVersionUID = 4707940896779281965L;
    
    /**
     * The userId of the account being requested
     */
    public final String userId;
    
    /**
     * The password of the account being requested
     */
    public final byte[] password;

    /**
     * Creates a new LoginPacket
     * @param userId the userId of the account being requested
     * @param password the password of the account being requested
     */
    public LoginPacket(String userId, byte[] password) {
        super(PacketId.LOGIN);
        this.userId = userId;
        this.password = password;
    }
    
}