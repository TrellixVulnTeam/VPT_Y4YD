package common.networking.packet.packets;

import common.networking.packet.PacketId;

/**
 * A {@link common.networking.packet.Packet} representing the status of the server
 */
public class ServerStatusPacket extends SingleDataPacket<ServerStatus> {
    
    private static final long serialVersionUID = -6508062814416496022L;
    
    /**
     * Creates a new ServerStatusPacket
     * @param status The status of the server
     */
    public ServerStatusPacket(ServerStatus status) {
        super(PacketId.SERVER_STATUS, status);
    }
    
}