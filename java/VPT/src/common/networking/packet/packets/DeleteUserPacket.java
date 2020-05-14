package common.networking.packet.packets;

import common.networking.packet.PacketId;

public class DeleteUserPacket extends SingleDataPacket<String> {

    private static final long serialVersionUID = -8508627265287531621L;
    
    public DeleteUserPacket(String userId) {
        super(PacketId.DELETE_USER, userId);
    }
    
}