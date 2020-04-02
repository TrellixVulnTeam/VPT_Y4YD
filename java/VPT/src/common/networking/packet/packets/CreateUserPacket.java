package common.networking.packet.packets;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

public class CreateUserPacket extends Packet {

    public final String userId;
    public final boolean isAdmin;
    public final byte[] password;

    public CreateUserPacket(String userId, boolean isAdmin, byte[] password) {
        super(PacketId.CREATE_USER);
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.password = password;
    }
    
}