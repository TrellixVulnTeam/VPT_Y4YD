package common.networking.packet.packets;

import common.networking.packet.PacketId;

public class DeleteUserPacket extends SingleDataPacket<String> {

    public DeleteUserPacket(String userId) {
        super(PacketId.DELETE_USER, userId);
    }
    
}