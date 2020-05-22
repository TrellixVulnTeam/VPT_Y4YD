package common.networking.packet.packets;

import common.networking.packet.PacketId;

/**
 * A {@link common.networking.packet.Packet} requesting the deletion of a user
 */
public class DeleteUserPacket extends SingleDataPacket<String> {

    private static final long serialVersionUID = -8508627265287531621L;
    
    /**
     * Creates a new DeleteUserPacket
     * @param userId the userId to request deleted
     */
    public DeleteUserPacket(String userId) {
        super(PacketId.DELETE_USER, userId);
    }
    
}