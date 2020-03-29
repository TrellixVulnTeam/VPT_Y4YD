package common.networking.packet.packets;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

public class LoginPacket extends Packet {
    
    public final String userId;
    public final byte[] password;

    public LoginPacket(String userId, byte[] password) {
        super(PacketId.LOGIN);
        this.userId = userId;
        this.password = password;
    }
    
}