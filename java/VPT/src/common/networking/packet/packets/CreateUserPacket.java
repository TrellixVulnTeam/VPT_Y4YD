package common.networking.packet.packets;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

public class CreateUserPacket extends Packet {

    private static final long serialVersionUID = 6527009738809022401L;
    
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