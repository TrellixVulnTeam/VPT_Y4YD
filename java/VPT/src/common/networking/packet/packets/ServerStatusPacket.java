package common.networking.packet.packets;

import common.networking.packet.PacketId;

public class ServerStatusPacket extends SingleDataPacket<ServerStatus> {
    
    private static final long serialVersionUID = -6508062814416496022L;
    
    public ServerStatusPacket(ServerStatus status) {
        super(PacketId.SERVER_STATUS, status);
    }
    
}