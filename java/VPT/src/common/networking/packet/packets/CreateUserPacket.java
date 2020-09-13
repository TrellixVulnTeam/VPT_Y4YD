package common.networking.packet.packets;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

/**
 * A {@link Packet} requesting the creation of a user
 */
public class CreateUserPacket extends Packet {

    private static final long serialVersionUID = 6527009738809022401L;
    
    /**
     * The requested userId of the new user
     */
    public final String userId;
    /**
     * The requested password of the new user
     */
    public final String password;

    /**
     * Creates a new CreateUserPacket
     * @param userId the requested userId of the new user
     * @param password the requested password of the new user
     */
    public CreateUserPacket(String userId, String password) {
        super(PacketId.CREATE_USER);
        this.userId = userId;
        this.password = password;
    }
    
}