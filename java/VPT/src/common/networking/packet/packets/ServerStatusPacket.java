package common.networking.packet.packets;

import common.networking.packet.PacketId;

public class ServerStatusPacket extends SingleDataPacket<ServerStatus> {
    
    public ServerStatusPacket(ServerStatus status) {
        super(PacketId.SERVER_STATUS, status);
    }
    
}