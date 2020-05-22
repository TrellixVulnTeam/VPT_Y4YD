package common.networking.packet.packets;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

/**
 * A {@link Packet} forcing the logout of a client
 */
public class ForceLogoutPacket extends Packet {

    private static final long serialVersionUID = -3820573605484580026L;
    
    /**
     * Creates a new ForceLogoutPacket
     */
    public ForceLogoutPacket() {
        super(PacketId.FORCE_LOGOUT);
    }
    
}