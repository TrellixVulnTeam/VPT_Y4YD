package common.networking.packet.packets;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

/**
 * A {@link Packet} requesting that the server shutdown
 */
public class ShutdownPacket extends Packet {

    private static final long serialVersionUID = 1251734832011348204L;
    
    /**
     * Creates a new ShutdownPacket
     */
    public ShutdownPacket() {
        super(PacketId.SHUTDOWN);
    }
    
}